package uk.co.captify;

import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.io.FileMatchers.anExistingFile;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.collections4.map.HashedMap;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;

import lombok.var;
import lombok.extern.slf4j.Slf4j;
import uk.co.captify.data.DataSaver;
import uk.co.captify.data.DataSaverFactory;
import uk.co.captify.data.Model;
import uk.co.captify.exceptions.UnableToLoadResource;
import uk.co.captify.exceptions.UnableToParseResource;
import uk.co.captify.parsers.CvsParser;
import uk.co.captify.parsers.IDataParser;
import uk.co.captify.parsers.writers.CvsWriter;
import uk.co.captify.parsers.writers.IDataWriter;
import uk.co.captify.utils.GzUnpack;
import uk.co.captify.utils.IUnpack;

/** Unit test for simple App. */
@Slf4j
public class AppTest {
  private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

  @SuppressWarnings("serial")
  @Test
  @Tag("Positive")
  void test_happyPath() throws IOException {
    var app = new App(inputFile, unpacker, parser, writer);
    assertAll(
        () ->
            assertEquals(
                new HashedMap<String, Long>() {
                  {
                    put("LAX", 4L);
                    put("KBP", 2L);
                    put("JFK", 0L);
                  }
                },
                app.get_planes_whole_period_arrived_to_each_airport()),
        () ->
            assertEquals(
                new HashedMap<String, Long>() {
                  {
                    put("LAX", 4L);
                    put("JFK", -4L);
                  }
                },
                app.get_planes_difference_arrived_left()),
        () ->
            assertEquals(
                new HashedMap<Integer, List<Model>>() {
                  {
                    put(
                        1,
                        new ArrayList<Model>() {
                          {
                            add(
                                new Model() {
                                  {
                                    setYEAR(2014);
                                    setQUARTER(1);
                                    setMONTH(1);
                                    setDAY_OF_MONTH(1);
                                    setDAY_OF_WEEK(3);
                                    setFL_DATE("2014-01-01");
                                    setORIGIN("JFK");
                                    setDEST("LAX");
                                  }
                                });
                            add(
                                new Model() {
                                  {
                                    setYEAR(2014);
                                    setQUARTER(1);
                                    setMONTH(1);
                                    setDAY_OF_MONTH(5);
                                    setDAY_OF_WEEK(7);
                                    setFL_DATE("2014-01-05");
                                    setORIGIN("JFK");
                                    setDEST("KBP");
                                  }
                                });
                          }
                        });
                    put(
                        2,
                        new ArrayList<Model>() {
                          {
                            add(
                                new Model() {
                                  {
                                    setYEAR(2014);
                                    setQUARTER(1);
                                    setMONTH(1);
                                    setDAY_OF_MONTH(6);
                                    setDAY_OF_WEEK(1);
                                    setFL_DATE("2014-01-06");
                                    setORIGIN("KBP");
                                    setDEST("LAX");
                                  }
                                });
                            add(
                                new Model() {
                                  {
                                    setYEAR(2014);
                                    setQUARTER(1);
                                    setMONTH(1);
                                    setDAY_OF_MONTH(8);
                                    setDAY_OF_WEEK(3);
                                    setFL_DATE("2014-01-08");
                                    setORIGIN("JFK");
                                    setDEST("LAX");
                                  }
                                });
                            add(
                                new Model() {
                                  {
                                    setYEAR(2014);
                                    setQUARTER(1);
                                    setMONTH(1);
                                    setDAY_OF_MONTH(12);
                                    setDAY_OF_WEEK(7);
                                    setFL_DATE("2014-01-12");
                                    setORIGIN("JFK");
                                    setDEST("KBP");
                                  }
                                });
                          }
                        });
                    put(
                        3,
                        new ArrayList<Model>() {
                          {
                            add(
                                new Model() {
                                  {
                                    setYEAR(2014);
                                    setQUARTER(1);
                                    setMONTH(1);
                                    setDAY_OF_MONTH(13);
                                    setDAY_OF_WEEK(1);
                                    setFL_DATE("2014-01-13");
                                    setORIGIN("KBP");
                                    setDEST("LAX");
                                  }
                                });
                          }
                        });
                  }
                },
                app.get_planes_per_week_arrived_to_each_airport(DATE_FORMAT)));
  }

  @Test
  @Tag("Positive")
  void test_relusts_saved() throws IOException {
    var resources =
        Arrays.asList(
            DataSaverFactory.DestPlanes(),
            DataSaverFactory.AirportDifference(),
            DataSaverFactory.WeekDestPlanes());

    for (var p : resources) {
      Files.deleteIfExists(Paths.get(p.build().getFile()));
    }

    var app = new App(inputFile, unpacker, parser, writer);
    var data = app.get_planes_difference_arrived_left();
    DataSaverFactory.AirportDifference().writer(writer).build().save(createData(data));

    var perWeekEachAirport = app.get_planes_per_week_arrived_to_each_airport(DATE_FORMAT);
    var rows = new ArrayList<String[]>();
    perWeekEachAirport.entrySet().stream()
        .forEach(
            e ->
                get_grouped_dest_airports(e.getValue(), app.airports).entrySet().stream()
                    .forEach(
                        e2 ->
                            rows.add(
                                new String[] {
                                  e.getKey().toString(), e2.getKey(), e2.getValue().toString()
                                })));
    DataSaverFactory.WeekDestPlanes().writer(writer).build().save(rows);

    data = app.get_planes_difference_arrived_left();
    DataSaverFactory.DestPlanes().writer(writer).build().save(createData(data));
    for (var p : resources) {
      assertThat(Paths.get(p.build().getFile()).toFile(), anExistingFile());
    }
  }

  private List<String[]> createData(Map<String, Long> data) {
    return data.entrySet().stream()
        .map(e -> new String[] {e.getKey(), e.getValue().toString()})
        .collect(toList());
  }

  private Map<String, Long> get_grouped_dest_airports(
      Collection<Model> collection, List<String> airports) {
    var result = collection.stream().collect(groupingBy(Model::getDEST, counting()));
    airports.forEach(e -> result.merge(e, 0L, Long::max));

    return result;
  }

  @Test
  @Tag("Negative")
  void test_wrong_date_format() throws IOException {
    var app = new App(inputFile, unpacker, parser, writer);
    assertThrows(
        IllegalArgumentException.class,
        () -> app.get_planes_per_week_arrived_to_each_airport(new SimpleDateFormat("blah")));
  }

  @SuppressWarnings("unused")
  private static Stream<Arguments> provideDifferentFileNames() {
    return Stream.of(
        Arguments.of(UnableToLoadResource.class, "/planes_log.csv"),
        Arguments.of(NullPointerException.class, null),
        Arguments.of(UnableToLoadResource.class, ""),
        Arguments.of(UnableToParseResource.class, "/log4j.properties"));
  }

  @ParameterizedTest
  @MethodSource("provideDifferentFileNames")
  @Tag("Negative")
  void test_data_file_not_found(Class<RuntimeException> exception, String file) {
    assertThrows(exception, () -> new App(file, unpacker, parser, writer));
  }

  // TODO tests with gz file content
  @Test
  @Tag("Negative")
  void incorrect_csv_header() throws IOException {
    var sourceFile = "./src/main/resources/incorrect_csv_header.csv";
    DataSaver.builder()
        .headers(
            Arrays.asList(
                "YEAR1",
                "QUARTER",
                "MONTH",
                "DAY_OF_MONTH",
                "DAY_OF_WEEK",
                "FL_DATE",
                "ORIGIN",
                "DEST"))
        .file(sourceFile)
        .writer(writer)
        .build()
        .save(new ArrayList<>());

    Files.deleteIfExists(Paths.get(sourceFile + ".gz"));
    gzipIt(sourceFile, sourceFile + ".gz");
    Files.deleteIfExists(Paths.get(sourceFile));

    var app = new App("/incorrect_csv_header.csv.gz", unpacker, parser, writer);
    assertAll(
        () -> assertEquals(new HashedMap<String, Long>(), app.get_planes_difference_arrived_left()),
        () ->
            assertEquals(
                new HashedMap<String, Long>(),
                app.get_planes_whole_period_arrived_to_each_airport()),
        () ->
            assertEquals(
                new HashedMap<Integer, List<Model>>(),
                app.get_planes_per_week_arrived_to_each_airport(DATE_FORMAT)));
  }

  @Test
  @Tag("Negative")
  @Tag("Mock")
  void mocked_unpacker() throws IOException {
    var mock = Mockito.mock(GzUnpack.class);
    doNothing().when(mock).unpack(Mockito.any(), Mockito.any());

    assertThrows(UnableToParseResource.class, () -> new App(inputFile, mock, parser, writer));
  }

  @Test
  @Tag("Negative")
  @Tag("Mock")
  void mocked_parser() throws IOException {
    @SuppressWarnings("unchecked")
    var mock = (CvsParser<Model>) Mockito.mock(CvsParser.class);
    when(mock.parse(Mockito.any(), Mockito.any())).thenReturn(null);

    assertThrows(NullPointerException.class, () -> new App(inputFile, unpacker, mock, writer));
  }

  @Test
  @Tag("Negative")
  @Tag("Mock")
  void mocked_writer() throws IOException {
    var mock = Mockito.mock(CvsWriter.class);
    doNothing().when(mock).write(Mockito.any(), Mockito.any());
    var file = Paths.get("./get_planes_difference_arrived_left.csv");
    Files.deleteIfExists(file);
    new App(inputFile, unpacker, parser, mock).get_planes_difference_arrived_left();
    assertFalse(Files.exists(file), file + " should not be present");
  }

  private final String inputFile = "/planes_log.csv.gz";

  private final IDataParser<Model> parser = new CvsParser<Model>();

  private final IUnpack unpacker = new GzUnpack();

  private final IDataWriter writer = new CvsWriter();

  private void gzipIt(String input, String out) {
    try (var bufferedWriter =
            new BufferedWriter(
                new OutputStreamWriter(new GZIPOutputStream(new FileOutputStream(out))));
        var bufferedReader = new BufferedReader(new FileReader(input)); ) {
      String line = null;
      //  from the input file to the GZIP output file
      while ((line = bufferedReader.readLine()) != null) {
        bufferedWriter.write(line);
        bufferedWriter.newLine();
      }
    } catch (IOException e) {
      log.error("unable to zip file: " + input + " to " + out);
    }
  }
}
