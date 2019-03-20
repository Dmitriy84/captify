package uk.co.captify;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
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
import uk.co.captify.data.Model;
import uk.co.captify.exceptions.UnableToLoadResource;
import uk.co.captify.exceptions.UnableToParseResource;
import uk.co.captify.parsers.CvsParser;
import uk.co.captify.parsers.IDataParser;
import uk.co.captify.parsers.writers.CvsWriter;
import uk.co.captify.parsers.writers.IDataWriter;
import uk.co.captify.utils.GzUnpack;
import uk.co.captify.utils.IUnpack;

/**
 * Unit test for simple App.
 */
public class AppTest {
	@SuppressWarnings("serial")
	@Test
	@Tag("Positive")
	void test_happyPath() throws IOException {
		var app = new App(inputFile, unpacker, parser, writer);
		assertAll(() -> assertEquals(new HashedMap<String, Long>() {
			{
				put("LAX", 4L);
				put("KBP", 2L);
				put("JFK", 0L);
			}
		}, app.get_planes_whole_period_arrived_to_each_airport()), () -> assertEquals(new HashedMap<String, Long>() {
			{
				put("LAX", 4L);
				put("JFK", -4L);
			}
		}, app.get_planes_difference_arrived_left()), () -> assertEquals(new HashedMap<Integer, List<Model>>() {
			{
				put(1, new ArrayList<Model>() {
					{
						add(new Model() {
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
						add(new Model() {
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
				put(2, new ArrayList<Model>() {
					{
						add(new Model() {
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
						add(new Model() {
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
						add(new Model() {
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
				put(3, new ArrayList<Model>() {
					{
						add(new Model() {
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
		}, app.get_planes_per_week_arrived_to_each_airport(new SimpleDateFormat("yyyy-MM-dd"))));
	}

	@Test
	@Tag("Negative")
	void test_wrong_date_format() throws IOException {
		var app = new App(inputFile, unpacker, parser, writer);
		assertThrows(IllegalArgumentException.class,
				() -> app.get_planes_per_week_arrived_to_each_airport(new SimpleDateFormat("blah")));
	}

	@SuppressWarnings("unused")
	private static Stream<Arguments> provideDifferentFileNames() {
		return Stream.of(//
				Arguments.of(UnableToLoadResource.class, "/planes_log.csv"),
				Arguments.of(NullPointerException.class, null), //
				Arguments.of(UnableToParseResource.class, ""),
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
		var h = Collections.singletonList(new String[] { "YEAR1", "QUARTER", "MONTH", "DAY_OF_MONTH", "DAY_OF_WEEK",
				"FL_DATE", "ORIGIN", "DEST" });
		List<String[]> data = new ArrayList<>();
		@SuppressWarnings("serial")
		List<String[]> rows = new ArrayList<String[]>(h.size() + data.size()) {
			{
				addAll(h);
				addAll(data);
			}
		};

		var sourceFile = "./src/main/resources/incorrect_csv_header.csv";
		writer.write(sourceFile, rows);

		Files.deleteIfExists(Paths.get(sourceFile + ".gz"));
		gzipIt(sourceFile, sourceFile + ".gz");
		Files.deleteIfExists(Paths.get(sourceFile));

		var app = new App("/incorrect_csv_header.csv.gz", unpacker, parser, writer);
		assertAll(() -> assertEquals(new HashedMap<String, Long>(), app.get_planes_difference_arrived_left()),
				() -> assertEquals(new HashedMap<String, Long>(),
						app.get_planes_whole_period_arrived_to_each_airport()),
				() -> assertEquals(new HashedMap<Integer, List<Model>>(),
						app.get_planes_per_week_arrived_to_each_airport(new SimpleDateFormat("yyyy-MM-dd"))));
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

	private void gzipIt(String input, String out) throws IOException {
		BufferedWriter bufferedWriter = null;
		BufferedReader bufferedReader = null;
		try {

			// Construct the BufferedWriter object
			bufferedWriter = new BufferedWriter(
					new OutputStreamWriter(new GZIPOutputStream(new FileOutputStream(out))));

			// Construct the BufferedReader object
			bufferedReader = new BufferedReader(new FileReader(input));

			String line = null;

			// from the input file to the GZIP output file
			while ((line = bufferedReader.readLine()) != null) {
				bufferedWriter.write(line);
				bufferedWriter.newLine();
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			// Close the BufferedWrter
			if (bufferedWriter != null) {
				try {
					bufferedWriter.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			// Close the BufferedReader
			if (bufferedReader != null) {
				try {
					bufferedReader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}