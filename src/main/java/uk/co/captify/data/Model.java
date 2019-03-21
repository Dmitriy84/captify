package uk.co.captify.data;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import com.opencsv.bean.CsvBindByName;

@Data
@ToString
@EqualsAndHashCode
public class Model {
	@CsvBindByName
	private int YEAR;
	@CsvBindByName
	private int QUARTER;
	@CsvBindByName
	private int MONTH;
	@CsvBindByName
	private int DAY_OF_MONTH;
	@CsvBindByName
	private int DAY_OF_WEEK;
	@CsvBindByName
	private String FL_DATE;
	@CsvBindByName
	private String ORIGIN;
	@CsvBindByName
	private String DEST;
}