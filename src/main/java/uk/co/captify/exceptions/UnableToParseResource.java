package uk.co.captify.exceptions;

import java.io.File;
import java.io.IOException;

public class UnableToParseResource extends RuntimeException {
	public UnableToParseResource(File out, IOException e) {
		super("Unable to parse unpacked resource: " + out, e);
	}

	private static final long serialVersionUID = -8972171712036116809L;
}