package uk.co.captify;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsNot.not;
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
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
import uk.co.captify.utils.AirportsUtils;
import uk.co.captify.utils.CsvUtils;
import uk.co.captify.utils.GzUnpack;
import uk.co.captify.utils.IUnpack;

/** Unit test for simple App. */
@Slf4j
public class AppTest {
  private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ISO_DATE;

  @SuppressWarnings("serial")
  @Test
  @Tag("Positive")
  void testHappyPath() throws IOException {
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
                app.getPlanesWholePeriodArrivedToEachAirport()),
        () ->
            assertEquals(
                new HashedMap<String, Long>() {
                  {
                    put("LAX", 4L);
                    put("JFK", -4L);
                  }
                },
                app.getPlanesDifferenceArrivedLeft()),
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
                                    setYear(2014);
                                    setQuarter(1);
                                    setMonth(1);
                                    setDayOfMonth(1);
                                    setDayOfWeek(3);
                                    setFlDate("2014-01-01");
                                    setOrigin("JFK");
                                    setDest("LAX");
                                  }
                                });
                            add(
                                new Model() {
                                  {
                                    setYear(2014);
                                    setQuarter(1);
                                    setMonth(1);
                                    setDayOfMonth(5);
                                    setDayOfWeek(7);
                                    setFlDate("2014-01-05");
                                    setOrigin("JFK");
                                    setDest("KBP");
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
                                    setYear(2014);
                                    setQuarter(1);
                                    setMonth(1);
                                    setDayOfMonth(6);
                                    setDayOfWeek(1);
                                    setFlDate("2014-01-06");
                                    setOrigin("KBP");
                                    setDest("LAX");
                                  }
                                });
                            add(
                                new Model() {
                                  {
                                    setYear(2014);
                                    setQuarter(1);
                                    setMonth(1);
                                    setDayOfMonth(8);
                                    setDayOfWeek(3);
                                    setFlDate("2014-01-08");
                                    setOrigin("JFK");
                                    setDest("LAX");
                                  }
                                });
                            add(
                                new Model() {
                                  {
                                    setYear(2014);
                                    setQuarter(1);
                                    setMonth(1);
                                    setDayOfMonth(12);
                                    setDayOfWeek(7);
                                    setFlDate("2014-01-12");
                                    setOrigin("JFK");
                                    setDest("KBP");
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
                                    setYear(2014);
                                    setQuarter(1);
                                    setMonth(1);
                                    setDayOfMonth(13);
                                    setDayOfWeek(1);
                                    setFlDate("2014-01-13");
                                    setOrigin("KBP");
                                    setDest("LAX");
                                  }
                                });
                          }
                        });
                  }
                },
                app.getPlanesPerWeekArrivedToEachAirport(DATE_FORMAT)));
  }

  @Test
  @Tag("Positive")
  void testRelustsSaved() throws IOException {
    var resources =
        Arrays.asList(
            DataSaverFactory.destPlanes(),
            DataSaverFactory.airportDifference(),
            DataSaverFactory.weekDestPlanes());

    for (var p : resources) {
      var path = Paths.get(p.build().getFile());
      Files.deleteIfExists(path);
      assertThat(path.toFile(), not(anExistingFile()));
    }

    var app = new App(inputFile, unpacker, parser, writer);
    var data = app.getPlanesDifferenceArrivedLeft();
    DataSaverFactory.airportDifference()
        .writer(writer)
        .build()
        .save(CsvUtils.createRowsForCsvFileFromMap(data));

    var rows = new ArrayList<String[]>();
    var perWeekEachAirport = app.getPlanesPerWeekArrivedToEachAirport(DATE_FORMAT);
    for (var e : perWeekEachAirport.entrySet()) {
      var airportsPerDest = AirportsUtils.getGroupedDestAirports(e.getValue(), app.airports);

      var rowsPerWeek =
          CsvUtils.createRowsForCsvFileFromMap(
              airportsPerDest,
              row -> new String[] {e.getKey().toString(), row.getKey(), row.getValue().toString()});
      log.debug("Per one week: " + rowsPerWeek);

      rows.addAll(rowsPerWeek);
    }

    DataSaverFactory.weekDestPlanes().writer(writer).build().save(rows);

    data = app.getPlanesDifferenceArrivedLeft();
    DataSaverFactory.destPlanes()
        .writer(writer)
        .build()
        .save(CsvUtils.createRowsForCsvFileFromMap(data));

    for (var p : resources) {
      assertThat(Paths.get(p.build().getFile()).toFile(), anExistingFile());
    }
  }

  @Test
  @Tag("Negative")
  void testWrongDateFormat() throws IOException {
    var app = new App(inputFile, unpacker, parser, writer);
    assertThrows(
        IllegalArgumentException.class,
        () -> app.getPlanesPerWeekArrivedToEachAirport(DateTimeFormatter.ofPattern("blah")));
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
  void testDataFileNotFound(Class<RuntimeException> exception, String file) {
    assertThrows(exception, () -> new App(file, unpacker, parser, writer));
  }

  // TODO tests with gz file content
  // TODO check data in saved cvs files
  @Test
  @Tag("Negative")
  void testIncorrectCsvHeader() throws IOException {
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
        () -> assertEquals(new HashedMap<String, Long>(), app.getPlanesDifferenceArrivedLeft()),
        () ->
            assertEquals(
                new HashedMap<String, Long>(), app.getPlanesWholePeriodArrivedToEachAirport()),
        () ->
            assertEquals(
                new HashedMap<Integer, List<Model>>(),
                app.getPlanesPerWeekArrivedToEachAirport(DATE_FORMAT)));
  }

  @Test
  @Tag("Negative")
  @Tag("Mock")
  void testMockedUnpacker() throws IOException {
    var mock = Mockito.mock(GzUnpack.class);
    doNothing().when(mock).unpack(Mockito.any(), Mockito.any());

    assertThrows(UnableToParseResource.class, () -> new App(inputFile, mock, parser, writer));
  }

  @Test
  @Tag("Negative")
  @Tag("Mock")
  void testMockedParser() throws IOException {
    @SuppressWarnings("unchecked")
    var mock = (CvsParser<Model>) Mockito.mock(CvsParser.class);
    when(mock.parse(Mockito.any(), Mockito.any())).thenReturn(null);

    assertThrows(NullPointerException.class, () -> new App(inputFile, unpacker, mock, writer));
  }

  @Test
  @Tag("Negative")
  @Tag("Mock")
  void testMockedWriter() throws IOException {
    var mock = Mockito.mock(CvsWriter.class);
    doNothing().when(mock).write(Mockito.any(), Mockito.any());
    var file = Paths.get("./get_planes_difference_arrived_left.csv");
    Files.deleteIfExists(file);
    new App(inputFile, unpacker, parser, mock).getPlanesDifferenceArrivedLeft();
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
