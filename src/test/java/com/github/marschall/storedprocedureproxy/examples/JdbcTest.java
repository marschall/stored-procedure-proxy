package com.github.marschall.storedprocedureproxy.examples;

import static org.junit.Assert.assertFalse;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;

public class JdbcTest extends AbstractExampleTest {

  @Override
  protected int plus1inout(int arg) throws SQLException {
    try (Connection connection = this.getDataSource().getConnection();
         CallableStatement statement = connection.prepareCall("{call plus1inout(?, ?)}")) {
        statement.setInt(1, arg);
        statement.registerOutParameter(2, Types.INTEGER);

        assertFalse(statement.execute());
        return statement.getInt(2);
    }
  }

}
