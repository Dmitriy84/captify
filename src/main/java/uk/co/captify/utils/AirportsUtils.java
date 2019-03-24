package uk.co.captify.utils;

import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;

import java.util.Collection;
import java.util.Map;

import lombok.var;
import uk.co.captify.data.Model;

public final class AirportsUtils {
  private AirportsUtils() {}

  public static Map<String, Long> getGroupedDestAirports(
      Collection<Model> collection, Iterable<String> airports) {
    var result = collection.stream().collect(groupingBy(Model::getDest, counting()));
    airports.forEach(e -> result.merge(e, 0L, Long::max));

    return result;
  }
}
