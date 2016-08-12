package com.github.marschall.springjdbccall;

import java.sql.SQLException;

/**
 * A {@link SQLExceptionAdapter} that creates a new {@link UncheckedSQLException}.
 */
final class UncheckedSQLExceptionAdapter implements SQLExceptionAdapter {

  static final SQLExceptionAdapter INSTANCE = new UncheckedSQLExceptionAdapter();

  private UncheckedSQLExceptionAdapter() {
    super();
  }

  @Override
  public RuntimeException translate(String procedureName, String sql, SQLException ex) {
    return new UncheckedSQLException("failed to call function '" + procedureName + "' with sql: " + sql, ex);
  }

}
