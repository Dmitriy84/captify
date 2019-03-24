package uk.co.captify.data;

import com.opencsv.bean.CsvBindByName;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@ToString
@EqualsAndHashCode
@AllArgsConstructor
public class Model {
  @CsvBindByName(column = "YEAR")
  private int year;

  @CsvBindByName(column = "QUARTER")
  private int quarter;

  @CsvBindByName(column = "MONTH")
  private int month;

  @CsvBindByName(column = "DAY_OF_MONTH")
  private int dayOfMonth;

  @CsvBindByName(column = "DAY_OF_WEEK")
  private int dayOfWeek;

  @CsvBindByName(column = "FL_DATE")
  private String flDate;

  @CsvBindByName(column = "ORIGIN")
  private String origin;

  @CsvBindByName(column = "DEST")
  private String dest;
}
