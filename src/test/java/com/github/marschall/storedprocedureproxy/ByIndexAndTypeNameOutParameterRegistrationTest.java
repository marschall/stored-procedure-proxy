package com.github.marschall.storedprocedureproxy;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.sql.Types;

import javax.sql.DataSource;

import org.junit.Before;
import org.junit.Test;

import com.github.marschall.storedprocedureproxy.annotations.OutParameter;
import com.github.marschall.storedprocedureproxy.annotations.ReturnValue;

public class ByIndexAndTypeNameOutParameterRegistrationTest {

  private CallableStatement callableStatement;

  private ReturnTypeNameUser procedures;

  @Before
  public void setUp() throws SQLException {
    DataSource dataSource = mock(DataSource.class);
    this.callableStatement = mock(CallableStatement.class);
    Connection connection = mock(Connection.class);
    DatabaseMetaData metaData = mock(DatabaseMetaData.class);

    when(dataSource.getConnection()).thenReturn(connection);
    when(connection.getMetaData()).thenReturn(metaData);
    when(metaData.getDatabaseProductName()).thenReturn("junit");
    when(connection.prepareCall(anyString())).thenReturn(this.callableStatement);
    procedures = ProcedureCallerFactory.build(ReturnTypeNameUser.class, dataSource);
  }

  @Test
  public void returnTypeNameOutParameter() throws SQLException {
    this.procedures.returnTypeNameOutParameter();
    verify(this.callableStatement).registerOutParameter(1, Types.VARCHAR, "duck");
  }

  @Test
  public void returnTypeNameFunction() throws SQLException {
    this.procedures.returnTypeNameFunction();
    verify(this.callableStatement).registerOutParameter(1, Types.VARCHAR, "dog");
  }

  @Test
  public void noReturnTypeNameOutParameter() throws SQLException {
    this.procedures.noReturnTypeNameOutParameter();
    verify(this.callableStatement).registerOutParameter(1, Types.VARCHAR);
  }

  @Test
  public void noReturnTypeNameFunction() throws SQLException {
    this.procedures.noReturnTypeNameFunction();
    verify(this.callableStatement).registerOutParameter(1, Types.VARCHAR);
  }

  interface ReturnTypeNameUser {

    @OutParameter(typeName = "duck")
    String returnTypeNameOutParameter();

    @ReturnValue(typeName = "dog")
    String returnTypeNameFunction();

    @OutParameter
    String noReturnTypeNameOutParameter();

    @ReturnValue
    String noReturnTypeNameFunction();

  }

}
