package uk.co.captify.data;

import java.io.IOException;
import java.util.List;

@FunctionalInterface
public interface IDataSaver {
  void save(List<? extends String[]> data) throws IOException;
}
