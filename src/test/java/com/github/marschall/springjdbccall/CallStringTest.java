package com.github.marschall.springjdbccall;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.junit.Before;
import org.junit.Test;

import com.github.marschall.springjdbccall.annotations.OutParameter;
import com.github.marschall.springjdbccall.annotations.ReturnValue;
import com.github.marschall.springjdbccall.spi.NamingStrategy;

public class CallStringTest {

  private DataSource dataSource;

  private Connection connection;

  @Before
  public void setUp() throws SQLException {
    dataSource = mock(DataSource.class);
    connection = mock(Connection.class);
    DatabaseMetaData metaData = mock(DatabaseMetaData.class);
    CallableStatement statement = mock(CallableStatement.class);

    when(dataSource.getConnection()).thenReturn(connection);
    when(connection.getMetaData()).thenReturn(metaData);
    when(metaData.getDatabaseProductName()).thenReturn("junit");
    when(connection.prepareCall(anyString())).thenReturn(statement);
    when(statement.execute()).thenReturn(false);
  }

  @Test
  public void noReturnValue() throws SQLException {
    // given

    SimpleProcedures procedures = ProcedureCallerFactory.of(SimpleProcedures.class, dataSource)
            .withProcedureNamingStrategy(NamingStrategy.snakeCase().thenLowerCase())
            .build();

    // when
    procedures.simpleFunction();

    verify(connection).prepareCall(eq("{call simple_function()}"));
  }

  @Test
  public void noReturnValueDynamicSchema() throws SQLException {
    // given

    SimpleProcedures procedures = ProcedureCallerFactory.of(SimpleProcedures.class, dataSource)
            .withProcedureNamingStrategy(NamingStrategy.snakeCase().thenLowerCase())
            .withSchemaNamingStrategy(ignored -> "dynamic")
            .build();

    // when
    procedures.simpleFunction();

    verify(connection).prepareCall(eq("{call dynamic.simple_function()}"));
  }

  @Test
  public void returnValue() throws SQLException {
    // given

    SimpleProcedures procedures = ProcedureCallerFactory.of(SimpleProcedures.class, dataSource)
            .withProcedureNamingStrategy(NamingStrategy.snakeCase().thenLowerCase())
            .build();

    // when
    procedures.returnValue();

    verify(connection).prepareCall(eq("{ ? = call return_value()}"));
  }

  @Test
  public void returnValueDynamicSchema() throws SQLException {
    // given

    SimpleProcedures procedures = ProcedureCallerFactory.of(SimpleProcedures.class, dataSource)
            .withProcedureNamingStrategy(NamingStrategy.snakeCase().thenLowerCase())
            .withSchemaNamingStrategy(ignored -> "dynamic")
            .build();

    // when
    procedures.returnValue();

    verify(connection).prepareCall(eq("{ ? = call dynamic.return_value()}"));
  }

  @Test
  public void outParameter() throws SQLException {
    // given

    SimpleProcedures procedures = ProcedureCallerFactory.of(SimpleProcedures.class, dataSource)
            .withProcedureNamingStrategy(NamingStrategy.snakeCase().thenLowerCase())
            .build();

    // when
    procedures.outParameter();

    verify(connection).prepareCall(eq("{call out_parameter(?)}"));
  }

  @Test
  public void outParameterDynamicSchema() throws SQLException {
    // given

    SimpleProcedures procedures = ProcedureCallerFactory.of(SimpleProcedures.class, dataSource)
            .withProcedureNamingStrategy(NamingStrategy.snakeCase().thenLowerCase())
            .withSchemaNamingStrategy(ignored -> "dynamic")
            .build();

    // when
    procedures.outParameter();

    verify(connection).prepareCall(eq("{call dynamic.out_parameter(?)}"));
  }

  interface SimpleProcedures {

    void simpleFunction();

    @ReturnValue
    String returnValue();

    @OutParameter
    String outParameter();

  }


}
