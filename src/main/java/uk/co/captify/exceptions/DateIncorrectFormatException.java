package uk.co.captify.exceptions;

public class DateIncorrectFormatException extends RuntimeException {

  public DateIncorrectFormatException(String date, Exception e) {
    super("Unable to parse: " + date, e);
  }

  private static final long serialVersionUID = 6093332143258889724L;
}
