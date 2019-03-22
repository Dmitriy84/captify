package uk.co.captify.data;

import java.io.IOException;
import java.util.List;

public interface IDataSaver {
  void save(List<String[]> data) throws IOException;
}
