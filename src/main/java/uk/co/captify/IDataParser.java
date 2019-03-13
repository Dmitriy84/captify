package uk.co.captify;

import java.util.List;

public interface IDataParser<T> {
	List<T> parse();
}