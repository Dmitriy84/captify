package uk.co.captify;

import java.io.Reader;
import java.util.List;

import com.opencsv.bean.CsvToBeanBuilder;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class CvsParser<T> implements IDataParser<T> {
	private Reader resource;
	private Class<T> model;

	public List<T> parse() {
		return new CsvToBeanBuilder<T>(resource).withType(model).withIgnoreLeadingWhiteSpace(true).build().parse();
	}
}