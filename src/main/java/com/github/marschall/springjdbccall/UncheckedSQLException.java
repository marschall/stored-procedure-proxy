package com.github.marschall.springjdbccall;

import java.sql.SQLException;

/**
 * Wraps an {@link SQLException} with an unchecked exception.
 *
 * @see java.io.UncheckedIOException
 */
public final class UncheckedSQLException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  UncheckedSQLException(SQLException cause) {
    super(cause);
  }

  UncheckedSQLException(String message, SQLException cause) {
    super(message, cause);
  }

}
