package uk.co.captify.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

import lombok.var;

public class GzUnpack implements IUnpack {
  public void unpack(InputStream input, File output) throws IOException {
    try (var in = new GZIPInputStream(input);
        var out = new FileOutputStream(output)) {
      byte[] buffer = new byte[1024];
      int len;
      while ((len = in.read(buffer)) != -1) {
        out.write(buffer, 0, len);
      }
    }
  }
}
