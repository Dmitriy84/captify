package uk.co.captify;

import static java.util.Collections.singleton;
import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.apache.log4j.PropertyConfigurator.configure;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Stream;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.math3.util.ArithmeticUtils;

import lombok.var;
import lombok.extern.slf4j.Slf4j;
import uk.co.captify.data.DataSaverFactory;
import uk.co.captify.data.Model;
import uk.co.captify.exceptions.DateIncorrectFormat;
import uk.co.captify.exceptions.UnableToLoadResource;
import uk.co.captify.exceptions.UnableToParseResource;
import uk.co.captify.parsers.CvsParser;
import uk.co.captify.parsers.IDataParser;
import uk.co.captify.parsers.writers.CvsWriter;
import uk.co.captify.parsers.writers.IDataWriter;
import uk.co.captify.utils.GzUnpack;
import uk.co.captify.utils.IUnpack;

@Slf4j
public class App {
  static {
    configure(App.class.getResourceAsStream("/log4j.properties"));
  }

  private final Collection<Model> data;

  public final List<String> airports;

  private final IDataWriter writer;

  public App(String in, IUnpack unpacker, IDataParser<Model> parser, IDataWriter writer)
      throws IOException {
    var resource = App.class.getClass().getResourceAsStream(in);
    if (resource == null) throw new UnableToLoadResource(in);

    String name = String.format("%s.%s", RandomStringUtils.randomAlphanumeric(8), "csv");
    var out = new File(new File("target"), name);

    log.debug("File unpacked to: " + out.getAbsolutePath());
    try {
      unpacker.unpack(resource, out);
      data = parser.parse(new BufferedReader(new FileReader(out)), Model.class);
    } catch (IOException e) {
      throw new UnableToParseResource(out, e);
    }
    airports =
        Stream.concat(data.stream().map(Model::getDest), data.stream().map(Model::getOrigin))
            .collect(toList());
    this.writer = writer;
  }

  public static void main(String[] args) throws IOException {
    for (var file : args) {
      var app = new App(file, new GzUnpack(), new CvsParser<Model>(), new CvsWriter());

      var message =
          "List of all airports with total number of planes for the whole period that arrived to each airport:\n{}\n";
      var planesWholePeriodEachAirport = app.getPlanesWholePeriodArrivedToEachAirport();

      DataSaverFactory.destPlanes()
          .writer(app.writer)
          .build()
          .save(
              planesWholePeriodEachAirport.entrySet().stream()
                  .map(e -> new String[] {e.getKey(), e.getValue().toString()})
                  .collect(toList()));
      log.info(message, planesWholePeriodEachAirport);

      message =
          "Non-Zero difference in total number of planes that arrived to and left from the airport:\n{}\n";
      var planesDifferenceArrivedLeft = app.getPlanesDifferenceArrivedLeft();

      DataSaverFactory.airportDifference()
          .writer(app.writer)
          .build()
          .save(
              planesDifferenceArrivedLeft.entrySet().stream()
                  .map(e -> new String[] {e.getKey(), e.getValue().toString()})
                  .collect(toList()));
      log.info(message, planesDifferenceArrivedLeft);

      log.info("Do the point 1 but sum number of planes separately per each week:");
      var planesPerWeekEachAirport =
          app.getPlanesPerWeekArrivedToEachAirport(new SimpleDateFormat("yyyy-MM-dd"));

      var data = new ArrayList<String[]>();
      planesPerWeekEachAirport.entrySet().stream()
          .forEach(
              e ->
                  app.getGroupedDestAirports(e.getValue()).entrySet().stream()
                      .forEach(
                          e2 ->
                              data.add(
                                  new String[] {
                                    e.getKey().toString(), e2.getKey(), e2.getValue().toString()
                                  })));

      DataSaverFactory.weekDestPlanes().writer(app.writer).build().save(data);
      planesPerWeekEachAirport
          .entrySet()
          .forEach(
              e -> log.info("Week #{}\n{}", e.getKey(), app.getGroupedDestAirports(e.getValue())));
    }
  }

  public Map<String, Long> getPlanesWholePeriodArrivedToEachAirport() {
    return getGroupedDestAirports(data);
  }

  public Map<String, Long> getPlanesDifferenceArrivedLeft() throws IOException {
    var streams =
        Stream.of(
            getGroupedDestAirports(data),
            data.stream().collect(groupingBy(Model::getOrigin, counting())));
    var results =
        streams
            .map(Map::entrySet)
            .flatMap(Collection::stream)
            .collect(toMap(Entry::getKey, Entry::getValue, ArithmeticUtils::subAndCheck));
    results.values().removeAll(singleton(0L));

    return results;
  }

  public Map<Integer, List<Model>> getPlanesPerWeekArrivedToEachAirport(SimpleDateFormat dateFormat)
      throws IOException {
    var results = data.stream().collect(groupingBy(m -> parseDate(dateFormat, m.getFlDate())));
    log.debug("per week data: " + results);

    return results;
  }

  private Integer parseDate(SimpleDateFormat dateFormat, String date) {
    var cal = Calendar.getInstance();
    try {
      cal.setTime(dateFormat.parse(date));
    } catch (ParseException e) {
      throw new DateIncorrectFormat(date, e);
    }

    return cal.get(Calendar.WEEK_OF_YEAR);
  }

  private Map<String, Long> getGroupedDestAirports(Collection<Model> collection) {
    var result = collection.stream().collect(groupingBy(Model::getDest, counting()));
    airports.forEach(e -> result.merge(e, 0L, Long::max));

    return result;
  }
}
