package uk.co.captify;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import com.opencsv.bean.CsvBindByName;

@Data
@ToString
@EqualsAndHashCode
public class Model {
    @CsvBindByName
    private float YEAR;
    @CsvBindByName
    private float QUARTER;
    @CsvBindByName
    private float MONTH;
    @CsvBindByName
    private float DAY_OF_MONTH;
    @CsvBindByName
    private float DAY_OF_WEEK;
    @CsvBindByName
    private String FL_DATE;
    @CsvBindByName
    private String ORIGIN;
    @CsvBindByName
    private String DEST;
}