package uk.co.captify.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

@FunctionalInterface
public interface IUnpack {
  void unpack(InputStream input, File output) throws IOException;
}
