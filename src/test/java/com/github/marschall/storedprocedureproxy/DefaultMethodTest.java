package com.github.marschall.storedprocedureproxy;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class DefaultMethodTest {

  private DataSource dataSource;

  private Connection connection;

  @BeforeEach
  public void setUp() throws SQLException {
    this.dataSource = mock(DataSource.class);
    this.connection = mock(Connection.class);
    DatabaseMetaData metaData = mock(DatabaseMetaData.class);
    CallableStatement statement = mock(CallableStatement.class);

    when(this.dataSource.getConnection()).thenReturn(this.connection);
    when(this.connection.getMetaData()).thenReturn(metaData);
    when(metaData.getDatabaseProductName()).thenReturn("junit");
    when(this.connection.prepareCall(anyString())).thenReturn(statement);
    when(statement.execute()).thenReturn(false);
  }



  @Test
  public void defaultMethod() throws SQLException {
    assumeFalse(isJava9OrLater());
    DefaultMethod defaultMethod = ProcedureCallerFactory.build(DefaultMethod.class, this.dataSource);

    assertThrows(IllegalStateException.class, () -> defaultMethod.hello());
  }



  private static boolean isJava9OrLater() {
    try {
      Class.forName("java.lang.Runtime$Version");
      return true;
    } catch (ClassNotFoundException e) {
      return false;
    }
  }

  interface DefaultMethod {

    default String hello() {
      return "world";
    }

  }

}
