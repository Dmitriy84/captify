package uk.co.captify;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import lombok.var;

import org.apache.commons.collections4.map.HashedMap;
import org.junit.jupiter.api.Test;

import uk.co.captify.data.Model;
import uk.co.captify.parsers.CvsParser;
import uk.co.captify.utils.GzUnpack;

/**
 * Unit test for simple App.
 */
public class AppTest {
	@SuppressWarnings("serial")
	@Test
	void happyPath() throws IOException {
		var app = new App("/planes_log.csv.gz", new GzUnpack(),
				new CvsParser<Model>());
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
}