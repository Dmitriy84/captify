package uk.co.captify;

import java.io.Reader;
import java.util.Collection;

import lombok.AllArgsConstructor;

import com.opencsv.bean.CsvToBeanBuilder;

@AllArgsConstructor
public class CvsParser<T> implements IDataParser<T> {
    private Reader resource;
    private Class<T> model;

    public Collection<T> parse() {
	return new CsvToBeanBuilder<T>(resource).withType(model)
		.withIgnoreLeadingWhiteSpace(true).build().parse();
    }
}