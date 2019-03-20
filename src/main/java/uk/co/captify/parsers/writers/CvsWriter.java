package uk.co.captify.parsers.writers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import com.opencsv.CSVWriter;

import lombok.var;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CvsWriter implements IDataWriter {
	public void write(String file, List<String[]> data) throws IOException {
		var path = Paths.get(file);
		log.debug("saving data to: " + path.toAbsolutePath());
		try (var writer = Files.newBufferedWriter(path);
				var csvWriter = new CSVWriter(writer, CSVWriter.DEFAULT_SEPARATOR, CSVWriter.NO_QUOTE_CHARACTER,
						CSVWriter.DEFAULT_ESCAPE_CHARACTER, CSVWriter.DEFAULT_LINE_END);) {
			csvWriter.writeAll(data);
		}
	}
}