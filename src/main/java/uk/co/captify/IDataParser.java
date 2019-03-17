package uk.co.captify;

import java.util.Collection;

public interface IDataParser<T> {
    Collection<T> parse();
}