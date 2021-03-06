package uk.co.captify.parsers.writers;

import java.io.IOException;
import java.util.List;

@FunctionalInterface
public interface IDataWriter {
  void write(String file, List<String[]> data) throws IOException;
}
