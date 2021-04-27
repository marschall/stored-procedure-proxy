package com.github.marschall.storedprocedureproxy;

import java.sql.SQLException;

import javax.sql.DataSource;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.UncategorizedSQLException;
import org.springframework.jdbc.support.SQLErrorCodeSQLExceptionTranslator;
import org.springframework.jdbc.support.SQLExceptionTranslator;

/**
 * A {@link SQLExceptionAdapter} that delegates to a {@link SQLExceptionTranslator}.
 */
final class SpringSQLExceptionAdapter implements SQLExceptionAdapter {

  private final SQLExceptionTranslator translator;

  SpringSQLExceptionAdapter(SQLExceptionTranslator translator) {
    this.translator = translator;
  }

  SpringSQLExceptionAdapter(DataSource dataSource) {
    // the same code that org.springframework.jdbc.support.JdbcAccessor#getExceptionTranslator() uses
    this(new SQLErrorCodeSQLExceptionTranslator(dataSource));
  }

  @Override
  public DataAccessException translate(String procedureName, String sql, SQLException ex) {
    DataAccessException translated = this.translator.translate("calling procedure " + procedureName, sql, ex);
    if (translated != null) {
      return translated;
    } else {
      // #translate may return null as per contract
      return new UncategorizedSQLException("failed to call procedure: " + procedureName, sql, ex);
    }
  }

}
