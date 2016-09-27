package com.github.marschall.storedprocedureproxy;

import java.sql.SQLException;

import javax.sql.DataSource;

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
  public RuntimeException translate(String procedureName, String sql, SQLException ex) {
    return this.translator.translate("calling procedure " + procedureName, sql, ex);
  }

}
