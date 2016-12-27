package com.github.marschall.storedprocedureproxy.examples;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;

import org.junit.Test;

public class JdbcTest extends AbstractExampleTest {

  @Test
  public void call() throws SQLException {
    assertEquals(2, plus1inout(1));
  }

  private int plus1inout(int argument) throws SQLException {
    try (Connection connection = this.getDataSource().getConnection();
         CallableStatement statement = connection.prepareCall("{call plus1inout(?, ?)}")) {
        statement.setInt(1, argument);
        statement.registerOutParameter(2, Types.INTEGER);

        assertFalse(statement.execute());
        return statement.getInt(2);
    }
  }

}
