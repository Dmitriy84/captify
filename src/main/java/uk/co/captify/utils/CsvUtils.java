package uk.co.captify.utils;

import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;

public final class CsvUtils {
  private CsvUtils() {}

  public static List<String[]> createRowsForCsvFileFromMap(Map<String, Long> data) {
    return createRowsForCsvFileFromMap(
        data, e -> new String[] {e.getKey(), e.getValue().toString()});
  }

  public static List<String[]> createRowsForCsvFileFromMap(
      Map<String, Long> data, Function<? super Entry<String, Long>, ? extends String[]> mapper) {
    return data.entrySet().stream().map(mapper).collect(toList());
  }
}
