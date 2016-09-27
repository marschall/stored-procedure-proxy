package com.github.marschall.storedprocedureproxy;


/**
 * Signals an unexpected amount of rows was returned.
 *
 * @see org.springframework.dao.IncorrectResultSizeDataAccessException
 */
public final class IncorrectResultSizeException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  IncorrectResultSizeException(int expectedSize, int actualSize) {
    super("Incorrect result size: expected " + expectedSize + ", but was " + actualSize);
  }

}
