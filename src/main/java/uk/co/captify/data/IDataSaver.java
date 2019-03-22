package uk.co.captify.data;

import java.io.IOException;
import java.util.List;

public interface IDataSaver {
  default void save(List<String[]> data) throws IOException {
    throw new UnsupportedOperationException("method should be overriden");
  };
}
