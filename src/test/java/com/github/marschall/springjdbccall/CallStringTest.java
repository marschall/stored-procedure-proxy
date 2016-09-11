package com.github.marschall.springjdbccall;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.junit.Test;

public class CallStringTest {

  @Test
  public void dynamicName() throws SQLException {
    // given
    DataSource dataSource = mock(DataSource.class);
    Connection connection = mock(Connection.class);
    DatabaseMetaData metaData = mock(DatabaseMetaData.class);
    CallableStatement statement = mock(CallableStatement.class);

    when(dataSource.getConnection()).thenReturn(connection);
    when(connection.getMetaData()).thenReturn(metaData);
    when(metaData.getDatabaseProductName()).thenReturn("junit");
    when(connection.prepareCall(anyString())).thenReturn(statement);
    when(statement.execute()).thenReturn(false);

    SimpleProcedures procedures = ProcedureCallerFactory.of(SimpleProcedures.class, dataSource)
            .withSchemaNamingStrategy(ignored -> "dynamic")
            .build();

    // when
    procedures.simpleFunction();
  }

  interface SimpleProcedures {

    void simpleFunction();

  }


}
