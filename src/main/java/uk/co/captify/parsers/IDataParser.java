package uk.co.captify.parsers;

import java.io.IOException;
import java.io.Reader;
import java.util.Collection;

@FunctionalInterface
public interface IDataParser<T> {
  Collection<T> parse(Reader resource, Class<T> model) throws IOException;
}
