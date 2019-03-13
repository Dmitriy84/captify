package uk.co.captify;

import com.opencsv.bean.CsvBindByName;

import lombok.Data;
import lombok.ToString;

@ToString
@Data
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