package com.github.marschall.storedprocedureproxy;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.junit.Before;
import org.junit.Test;

import com.github.marschall.storedprocedureproxy.ProcedureCallerFactory;
import com.github.marschall.storedprocedureproxy.annotations.FetchSize;
import com.github.marschall.storedprocedureproxy.annotations.OutParameter;

public class FetchSizeTest {

  private DataSource dataSource;

  private CallableStatement statement;

  @Before
  public void setUp() throws SQLException {
    dataSource = mock(DataSource.class);
    Connection connection = mock(Connection.class);
    DatabaseMetaData metaData = mock(DatabaseMetaData.class);
    statement = mock(CallableStatement.class);
    ResultSet resultSet = mock(ResultSet.class);

    when(dataSource.getConnection()).thenReturn(connection);
    when(connection.getMetaData()).thenReturn(metaData);
    when(metaData.getDatabaseProductName()).thenReturn("junit");
    when(connection.prepareCall(anyString())).thenReturn(statement);
    when(statement.execute()).thenReturn(false);
    when(statement.getObject(1, ResultSet.class)).thenReturn(resultSet);
    when(resultSet.next()).thenReturn(false);
  }

  @Test
  public void defaultFetchSize() throws SQLException {
    // given

    CustomFetchSize procedures = ProcedureCallerFactory.build(CustomFetchSize.class, dataSource);

    // when
    procedures.defaultFetchSize();

    // then
    verify(statement).setFetchSize(10);
  }

  @Test
  public void customFetchSize() throws SQLException {
    // given

    CustomFetchSize procedures = ProcedureCallerFactory.build(CustomFetchSize.class, dataSource);

    // when
    procedures.customFetchSize();

    // then
    verify(statement).setFetchSize(20);
  }

  @Test
  public void noFetchSize() throws SQLException {
    // given

    NoFetchSize procedures = ProcedureCallerFactory.build(NoFetchSize.class, dataSource);

    // when
    procedures.noFetchSize();

    // then
    verify(statement, never()).setFetchSize(anyInt());
  }

  @FetchSize(10)
  interface CustomFetchSize {

    @OutParameter
    List<String> defaultFetchSize();

    @FetchSize(20)
    @OutParameter
    List<String> customFetchSize();

  }

  interface NoFetchSize {

    @OutParameter
    List<String> noFetchSize();

  }

}
