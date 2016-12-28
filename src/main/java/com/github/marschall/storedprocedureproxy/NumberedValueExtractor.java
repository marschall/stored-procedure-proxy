package com.github.marschall.storedprocedureproxy;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Extracts a value from a single row.
 *
 * <p>This class is used to extract a value object from every row in a
 * ref cursor out parameter.</p>
 *
 * <p>This class is modeled after Springs
 * {@link org.springframework.jdbc.core.RowMapper}. If you're using
 * lambdas the the code should directly port over. If not the easiest
 * way to bridge the code is using an method reference.</p>
 *
 * <p>Implementations should not catch {@link SQLException} this will
 * be done by a higher layer.</p>
 *
 * @param <V> the value type
 * @see org.springframework.jdbc.core.RowMapper
 */
@FunctionalInterface
public interface NumberedValueExtractor<V> {

  /**
   * Extract the value from the current row.
   *
   * <p>Implementations should not call {@link ResultSet#next()} but
   * instead expect to be called for every method.
   *
   * @param resultSet
   *          the ResultSet to the value of the current row from
   * @param rowNumber
   *          the 0-based index of the current row, mostly for Spring compatibility
   * @return the value for the current row
   * @throws SQLException
   *           propagated with a method on {@link ResultSet} throws an exception
   * @see org.springframework.jdbc.core.RowMapper#mapRow(ResultSet, int)
   */
  V extractValue(ResultSet resultSet, int rowNumber) throws SQLException;

}
