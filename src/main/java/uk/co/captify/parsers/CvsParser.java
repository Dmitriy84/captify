package uk.co.captify.parsers;

import java.io.IOException;
import java.io.Reader;
import java.util.Collection;

import com.opencsv.bean.CsvToBeanBuilder;

public class CvsParser<T> implements IDataParser<T> {
  public Collection<T> parse(Reader resource, Class<T> model) throws IOException {
    try (Reader temp = resource) {
      return new CsvToBeanBuilder<T>(resource)
          .withType(model)
          .withIgnoreLeadingWhiteSpace(true)
          .build()
          .parse();
    }
  }
}
