package uk.co.captify;

import static java.time.temporal.WeekFields.ISO;
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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
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
import uk.co.captify.exceptions.UnableToLoadResource;
import uk.co.captify.exceptions.UnableToParseResource;
import uk.co.captify.parsers.CvsParser;
import uk.co.captify.parsers.IDataParser;
import uk.co.captify.parsers.writers.CvsWriter;
import uk.co.captify.parsers.writers.IDataWriter;
import uk.co.captify.utils.AirportsUtils;
import uk.co.captify.utils.CsvUtils;
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
    if (resource == null) {
      throw new UnableToLoadResource(in);
    }

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

  public static void main(String[] args) {
    try {
      for (var file : args) {
        var app = new App(file, new GzUnpack(), new CvsParser<Model>(), new CvsWriter());

        var message =
            "List of all airports with total number of planes for the whole period that arrived to each airport:\n{}\n";
        var planesWholePeriodEachAirport = app.getPlanesWholePeriodArrivedToEachAirport();

        DataSaverFactory.destPlanes()
            .writer(app.writer)
            .build()
            .save(CsvUtils.createRowsForCsvFileFromMap(planesWholePeriodEachAirport));
        log.info(message, planesWholePeriodEachAirport);

        message =
            "Non-Zero difference in total number of planes that arrived to and left from the airport:\n{}\n";
        var planesDifferenceArrivedLeft = app.getPlanesDifferenceArrivedLeft();

        DataSaverFactory.airportDifference()
            .writer(app.writer)
            .build()
            .save(CsvUtils.createRowsForCsvFileFromMap(planesDifferenceArrivedLeft));
        log.info(message, planesDifferenceArrivedLeft);

        log.info("Do the point 1 but sum number of planes separately per each week:");
        var planesPerWeekEachAirport =
            app.getPlanesPerWeekArrivedToEachAirport(DateTimeFormatter.ISO_DATE);

        var data = new ArrayList<String[]>();
        for (var e : planesPerWeekEachAirport.entrySet()) {
          var airportsPerDest = AirportsUtils.getGroupedDestAirports(e.getValue(), app.airports);
          log.info("Week #{}\n{}", e.getKey(), airportsPerDest);

          var rowsPerWeek =
              CsvUtils.createRowsForCsvFileFromMap(
                  airportsPerDest,
                  row ->
                      new String[] {
                        e.getKey().toString(), row.getKey(), row.getValue().toString()
                      });
          log.debug("Per one week: " + rowsPerWeek);

          data.addAll(rowsPerWeek);
        }

        DataSaverFactory.weekDestPlanes().writer(app.writer).build().save(data);
      }
    } catch (Exception t) {
      log.error("Fatal error", t);
      System.exit(1);
    }
  }

  public Map<String, Long> getPlanesWholePeriodArrivedToEachAirport() {
    return AirportsUtils.getGroupedDestAirports(data, airports);
  }

  public Map<String, Long> getPlanesDifferenceArrivedLeft() throws IOException {
    var streams =
        Stream.of(
            AirportsUtils.getGroupedDestAirports(data, airports),
            data.stream().collect(groupingBy(Model::getOrigin, counting())));
    var results =
        streams
            .map(Map::entrySet)
            .flatMap(Collection::stream)
            .collect(toMap(Entry::getKey, Entry::getValue, ArithmeticUtils::subAndCheck));
    results.values().removeAll(singleton(0L));

    return results;
  }

  public Map<Integer, List<Model>> getPlanesPerWeekArrivedToEachAirport(
      DateTimeFormatter dateFormat) throws IOException {
    var results =
        data.stream()
            .collect(
                groupingBy(m -> LocalDate.parse(m.getFlDate(), dateFormat).get(ISO.weekOfYear())));
    log.debug("per week data: " + results);

    return results;
  }
}
