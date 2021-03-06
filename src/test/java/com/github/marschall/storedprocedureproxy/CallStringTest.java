package com.github.marschall.storedprocedureproxy;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.github.marschall.storedprocedureproxy.annotations.InOutParameter;
import com.github.marschall.storedprocedureproxy.annotations.OutParameter;
import com.github.marschall.storedprocedureproxy.annotations.ReturnValue;
import com.github.marschall.storedprocedureproxy.spi.NamingStrategy;

public class CallStringTest {

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
  public void inOutParameter() throws SQLException {
    // given

    SimpleProcedures procedures = ProcedureCallerFactory.of(SimpleProcedures.class, dataSource)
            .withProcedureNamingStrategy(NamingStrategy.snakeCase().thenLowerCase())
            .build();

    // when
    procedures.inOutParameter("test");

    verify(connection).prepareCall(eq("{call in_out_parameter(?)}"));
  }

  @Test
  public void valueExtractor() throws SQLException {
    // given
    this.dataSource = mock(DataSource.class);
    this.connection = mock(Connection.class);
    ResultSet resultSet = mock(ResultSet.class);
    DatabaseMetaData metaData = mock(DatabaseMetaData.class);
    CallableStatement statement = mock(CallableStatement.class);

    when(this.dataSource.getConnection()).thenReturn(this.connection);
    when(this.connection.getMetaData()).thenReturn(metaData);
    when(metaData.getDatabaseProductName()).thenReturn("junit");
    when(this.connection.prepareCall(anyString())).thenReturn(statement);
    when(statement.execute()).thenReturn(true);
    when(statement.getResultSet()).thenReturn(resultSet);
    when(resultSet.next()).thenReturn(false);

    SimpleProcedures procedures = ProcedureCallerFactory.of(SimpleProcedures.class, dataSource)
            .withProcedureNamingStrategy(NamingStrategy.snakeCase().thenLowerCase())
            .build();

    // when
    procedures.valueExtractor(rs -> "ok");

    verify(connection).prepareCall(eq("{call value_extractor()}"));
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

  @Test
  public void returnValueAndNamespace() throws SQLException {
    // given

    SimpleProcedures procedures = ProcedureCallerFactory.of(SimpleProcedures.class, dataSource)
            .withProcedureNamingStrategy(NamingStrategy.snakeCase().thenLowerCase())
            .withNamespaceNamingStrategy(ignored -> "scope")
            .build();

    // when
    procedures.returnValue();

    verify(connection).prepareCall(eq("{ ? = call scope.return_value()}"));
  }

  @Test
  public void returnValueDynamicSchemaAndNamespace() throws SQLException {
    // given

    SimpleProcedures procedures = ProcedureCallerFactory.of(SimpleProcedures.class, dataSource)
            .withProcedureNamingStrategy(NamingStrategy.snakeCase().thenLowerCase())
            .withSchemaNamingStrategy(ignored -> "dynamic")
            .withNamespaceNamingStrategy(ignored -> "scope")
            .build();

    // when
    procedures.returnValue();

    verify(connection).prepareCall(eq("{ ? = call scope.dynamic.return_value()}"));
  }

  @Test
  public void outParameterAndNamespace() throws SQLException {
    // given

    SimpleProcedures procedures = ProcedureCallerFactory.of(SimpleProcedures.class, dataSource)
            .withProcedureNamingStrategy(NamingStrategy.snakeCase().thenLowerCase())
            .withNamespaceNamingStrategy(ignored -> "scope")
            .build();

    // when
    procedures.outParameter();

    verify(connection).prepareCall(eq("{call scope.out_parameter(?)}"));
  }

  @Test
  public void outParameterDynamicSchemaAndNamespace() throws SQLException {
    // given

    SimpleProcedures procedures = ProcedureCallerFactory.of(SimpleProcedures.class, dataSource)
            .withProcedureNamingStrategy(NamingStrategy.snakeCase().thenLowerCase())
            .withSchemaNamingStrategy(ignored -> "dynamic")
            .withNamespaceNamingStrategy(ignored -> "scope")
            .build();

    // when
    procedures.outParameter();

    verify(connection).prepareCall(eq("{call scope.dynamic.out_parameter(?)}"));
  }

  interface SimpleProcedures {

    void simpleFunction();

    @ReturnValue
    String returnValue();

    @OutParameter
    String outParameter();

    @InOutParameter
    String inOutParameter(String s);

    List<String> valueExtractor(ValueExtractor<String> extractor);

  }

}
