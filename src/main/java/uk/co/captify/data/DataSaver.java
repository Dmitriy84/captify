package uk.co.captify.data;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Singular;
import lombok.var;
import uk.co.captify.parsers.writers.IDataWriter;

@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DataSaver implements IDataSaver {
  @Singular private List<String> headers;
  @Getter private String file;
  private IDataWriter writer;

  @Override
  public void save(List<? extends String[]> data) throws IOException {
    var h = Collections.singletonList(headers.toArray(new String[] {}));

    var rows = new ArrayList<String[]>(h.size() + data.size());
    rows.addAll(h);
    rows.addAll(data);

    writer.write(file, rows);
  }
}
