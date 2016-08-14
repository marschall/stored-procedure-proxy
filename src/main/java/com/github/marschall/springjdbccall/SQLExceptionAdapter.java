package com.github.marschall.springjdbccall;

import java.sql.SQLException;

/**
 * Translates a checked {@link SQLException} to an unchecked exception.
 *
 * <p>Very similar to {@link org.springframework.jdbc.support.SQLExceptionTranslator}.</p>
 */
@FunctionalInterface
public interface SQLExceptionAdapter {

  RuntimeException translate(String procedureName, String sql, SQLException ex);

}
