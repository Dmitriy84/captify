package uk.co.captify.parsers;

import java.io.Reader;
import java.util.Collection;

public interface IDataParser<T> {
	Collection<T> parse(Reader resource, Class<T> model);
}