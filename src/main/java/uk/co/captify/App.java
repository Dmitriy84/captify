package uk.co.captify;

import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Stream;

import lombok.var;

import org.apache.commons.math3.util.ArithmeticUtils;

public class App {
    private final Collection<Model> data;
    private final List<String> airports;

    public App(Collection<Model> data) {
	this.data = data;
	airports = Stream.concat(data.stream().map(Model::getDEST),
		data.stream().map(Model::getORIGIN)).collect(toList());
    }

    public static void main(String[] args) throws IOException {
	for (var a : args) {
	    var data = App.class.getResourceAsStream(a);
	    if (data == null)
		throw new FileNotFoundException("Unable to load resource: "
			+ args[0]);

	    var file = new File(a);
	    var i = file.getName().lastIndexOf('.');
	    var new_name = "target/" + file.getName().substring(0, i);
	    Utils.unpack_gz(data, new File(new_name));

	    var parsed_data = new CvsParser<Model>(new BufferedReader(
		    new FileReader(new_name)), Model.class).parse();
	    var app = new App(parsed_data);

	    var message = "List of all airports with total number of planes for the whole period that arrived to each airport:%n%s%n%n";
	    var planes_whole_period_each_airport = app
		    .get_planes_whole_period_arrived_to_each_airport();
	    System.out.printf(message, planes_whole_period_each_airport);

	    message = "Non-Zero difference in total number of planes that arrived to and left from the airport:%n%s%n%n";
	    var planes_difference_arrived_left = app
		    .get_planes_difference_arrived_left();
	    System.out.printf(message, planes_difference_arrived_left);

	    System.out
		    .printf("Do the point 1 but sum number of planes separately per each week:%n");
	    var planes_per_week_each_airport = app
		    .get_planes_per_week_arrived_to_each_airport(new SimpleDateFormat(
			    "yyyy-MM-dd"));
	    planes_per_week_each_airport.entrySet().forEach(
		    e -> System.out.printf("Week #%s%n%s%n", e.getKey(),
			    app.get_dest_airports(e.getValue())));
	}
    }

    public Map<String, Long> get_planes_whole_period_arrived_to_each_airport() {
	return get_dest_airports(data);
    }

    public Map<String, Long> get_planes_difference_arrived_left() {
	var result = Stream
		.of(get_dest_airports(data),
			data.stream().collect(
				groupingBy(Model::getORIGIN, counting())))
		.map(Map::entrySet)
		.flatMap(Collection::stream)
		.collect(
			toMap(Entry::getKey, Entry::getValue,
				ArithmeticUtils::subAndCheck));
	result.values().removeAll(Collections.singleton(0L));

	return result;
    }

    public Map<Integer, List<Model>> get_planes_per_week_arrived_to_each_airport(
	    SimpleDateFormat dateFormat) {
	var cal = Calendar.getInstance();
	var result = data.stream().collect(groupingBy(m -> {
	    try {
		cal.setTime(dateFormat.parse(m.getFL_DATE()));
	    } catch (ParseException e) {
		throw new RuntimeException(e);
	    }
	    return cal.get(Calendar.WEEK_OF_YEAR);
	}));

	return result;
    }

    private Map<String, Long> get_dest_airports(Collection<Model> collection) {
	var result = collection.stream().collect(
		groupingBy(Model::getDEST, counting()));
	airports.forEach(e -> result.merge(e, 0L, Long::max));

	return result;
    }
}