package uk.co.captify;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import lombok.var;

import org.apache.commons.collections4.map.HashedMap;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;

import uk.co.captify.data.Model;
import uk.co.captify.exceptions.UnableToLoadResource;
import uk.co.captify.exceptions.UnableToParseResource;
import uk.co.captify.parsers.CvsParser;
import uk.co.captify.utils.GzUnpack;

/**
 * Unit test for simple App.
 */
public class AppTest {
	@SuppressWarnings("serial")
	@Test
	@Tag("Positive")
	void test_happyPath() throws IOException {
		var app = new App(inputFile, unpacker, parser);
		assertAll(
				() -> assertEquals(new HashedMap<String, Long>() {
					{
						put("LAX", 4L);
						put("KBP", 2L);
						put("JFK", 0L);
					}
				}, app.get_planes_whole_period_arrived_to_each_airport()),
				() -> assertEquals(new HashedMap<String, Long>() {
					{
						put("LAX", 4L);
						put("JFK", -4L);
					}
				}, app.get_planes_difference_arrived_left()),
				() -> assertEquals(
						new HashedMap<Integer, List<Model>>() {
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
						},
						app.get_planes_per_week_arrived_to_each_airport(new SimpleDateFormat(
								"yyyy-MM-dd"))));
	}

	@Test
	@Tag("Negative")
	void test_wrong_date_format() throws IOException {
		var app = new App(inputFile, unpacker, parser);
		assertThrows(
				IllegalArgumentException.class,
				() -> app
						.get_planes_per_week_arrived_to_each_airport(new SimpleDateFormat(
								"blah")));
	}

	@SuppressWarnings("unused")
	private static Stream<Arguments> provideDifferentFileNames() {
		return Stream.of(
				Arguments.of(UnableToLoadResource.class, "/planes_log.csv"),
				Arguments.of(NullPointerException.class, null),
				Arguments.of(UnableToParseResource.class, ""),
				Arguments.of(UnableToParseResource.class, "/log4j.properties"));
	}

	@ParameterizedTest
	@MethodSource("provideDifferentFileNames")
	@Tag("Negative")
	void test_data_file_not_found(Class<RuntimeException> exception, String file) {
		assertThrows(exception, () -> new App(file, unpacker, parser));
	}

	// TODO tests with gz file content

	@Test
	@Tag("Negative")
	@Tag("Mock")
	void mocked_unpacker() throws IOException {
		var mock = Mockito.mock(GzUnpack.class);
		doNothing().when(mock).unpack(Mockito.any(), Mockito.any());

		assertThrows(UnableToParseResource.class, () -> new App(inputFile,
				mock, parser));
	}

	@Test
	@Tag("Negative")
	@Tag("Mock")
	void mocked_parser() throws IOException {
		@SuppressWarnings("unchecked")
		var mock = (CvsParser<Model>) Mockito.mock(CvsParser.class);
		when(mock.parse(Mockito.any(), Mockito.any())).thenReturn(null);

		assertThrows(NullPointerException.class, () -> new App(inputFile,
				unpacker, mock));
	}

	private final String inputFile = "/planes_log.csv.gz";
	private final CvsParser<Model> parser = new CvsParser<Model>();
	private final GzUnpack unpacker = new GzUnpack();
}