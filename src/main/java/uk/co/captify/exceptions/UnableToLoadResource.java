package uk.co.captify.exceptions;

public class UnableToLoadResource extends RuntimeException {
  public UnableToLoadResource(String string) {
    super("Unable to load resource: " + string);
  }

  private static final long serialVersionUID = 4122643917151344701L;
}
