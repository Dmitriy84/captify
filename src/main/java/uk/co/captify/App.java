package uk.co.captify;

import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toMap;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import lombok.var;

public class App {
	public static void main(String[] args) throws IOException {
		var app = new App();

		var data = app.getClass().getResourceAsStream(args[0]);

		app.work(new CvsParser<Model>(new BufferedReader(new InputStreamReader(data)), Model.class));

	}

	public void work(IDataParser<Model> parser) {
		var data = parser.parse();
		data.forEach(System.out::println);

		System.out.println(
				" List of all airports with total number of planes for the whole period that arrived to each airport: "
						+ groupAndCount(data) + "\n");

		var res2 = new HashMap<String, Long>(groupAndCount(data));
		data.stream().collect(groupingBy(Model::getORIGIN, counting()))
				.forEach((k, v) -> res2.put(k, (res2.containsKey(k) ? res2.get(k) : 0) - v));
		var collect = res2.entrySet().stream().filter(e -> e.getValue() != 0)
				.collect(toMap(Entry::getKey, Entry::getValue));
		System.out.println("Non-Zero difference in total number of planes that arrived to and left from the airport: "
				+ collect + "\n");

		var cal = Calendar.getInstance();
		var df = new SimpleDateFormat("yyyy-MM-dd");
		var res3 = data.stream().collect(groupingBy(m -> {
			try {
				cal.setTime(df.parse(m.getFL_DATE()));
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return 0;
			}
			return cal.get(Calendar.WEEK_OF_YEAR);
		}));
		res3.entrySet().forEach(e -> System.out.println("Week #" + e.getKey() + "\n" + groupAndCount(e.getValue())));
		System.out.println("Do the point 1 but sum number of planes separately per each week: " + res3 + "\n");
	}

	private Map<String, Long> groupAndCount(Collection<Model> collection) {
		return collection.stream().collect(groupingBy(Model::getDEST, counting()));
	}
}