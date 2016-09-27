package com.github.marschall.storedprocedureproxy;

import java.sql.SQLException;
import java.util.Objects;

/**
 * Wraps an {@link SQLException} with an unchecked exception.
 *
 * @see java.io.UncheckedIOException
 */
public final class UncheckedSQLException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  UncheckedSQLException(SQLException cause) {
    super(Objects.requireNonNull(cause));
  }

  UncheckedSQLException(String message, SQLException cause) {
    super(message, Objects.requireNonNull(cause));
  }

  /**
   * Convenience method that returns the cause as type {@link SQLException}
   * avoiding the need to cast the result.
   *
   * @return the exception cause, never {@code null}
   */
  @Override
  public SQLException getCause() {
    return (SQLException) super.getCause();
  }

}
