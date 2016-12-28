package com.github.marschall.storedprocedureproxy;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Extracts a value from a single row.
 *
 * <p>This class is used to extract a value object from every row in a
 * ref cursor out parameter.</p>
 *
 * <p>Implementations should not catch {@link SQLException} this will
 * be done by a higher layer.</p>
 *
 * @param <V> the value type
 * @see NumberedValueExtractor
 */
@FunctionalInterface
public interface ValueExtractor<V> {

  /**
   * Extract the value from the current row.
   *
   * <p>Implementations should not call {@link ResultSet#next()} but
   * instead expect to be called for every method.
   *
   * @param resultSet the ResultSet to the value of the current row from
   * @return the value for the current row
   * @throws SQLException propagated if a method on {@link ResultSet} throws an exception
   */
  V extractValue(ResultSet resultSet) throws SQLException;

}
