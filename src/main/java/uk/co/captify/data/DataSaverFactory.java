package uk.co.captify.data;

import uk.co.captify.data.DataSaver.DataSaverBuilder;

public class DataSaverFactory {
  public static DataSaverBuilder DestPlanes() {
    return DataSaver.builder()
        .header("DEST")
        .header("PLANES")
        .file("./get_planes_whole_period_arrived_to_each_airport.csv");
  }

  public static DataSaverBuilder AirportDifference() {
    return DataSaver.builder()
        .header("AIRPORT")
        .header("DIFFERENCE")
        .file("./get_planes_difference_arrived_left.csv");
  }

  public static DataSaverBuilder WeekDestPlanes() {
    return DataSaver.builder()
        .header("WEEK")
        .header("DEST")
        .header("PLANES")
        .file("./get_planes_per_week_arrived_to_each_airport.csv");
  }
}
