package com.github.marschall.storedprocedureproxy;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.junit.Test;

import com.github.marschall.storedprocedureproxy.ProcedureCallerFactory.ProcedureCaller;

public class ValueExtractorResultExtractorTest {

  @Test
  public void valueExtractorArguments() throws SQLException {
    // set up
    DataSource dataSource = mock(DataSource.class);
    CallableStatement callableStatement = mock(CallableStatement.class);
    Connection connection = mock(Connection.class);
    DatabaseMetaData metaData = mock(DatabaseMetaData.class);
    ResultSet resultSet = mock(ResultSet.class);
    ValueExtractor<String> valueExtractor = mock(ValueExtractor.class);
    SampleInterface procedures = ProcedureCallerFactory.build(SampleInterface.class, dataSource);

    when(dataSource.getConnection()).thenReturn(connection);
    when(connection.getMetaData()).thenReturn(metaData);
    when(metaData.getDatabaseProductName()).thenReturn("junit");
    when(connection.prepareCall(anyString())).thenReturn(callableStatement);
    when(callableStatement.execute()).thenReturn(true);
    when(callableStatement.getResultSet()).thenReturn(resultSet);

    // actual behavior
    when(resultSet.next()).thenReturn(true, true, false);
    when(valueExtractor.extractValue(eq(resultSet), anyInt())).thenReturn("s");

    // when
    procedures.extractString(valueExtractor);

    // then
    verify(valueExtractor, times(1)).extractValue(resultSet, 0);
    verify(valueExtractor, times(1)).extractValue(resultSet, 1);
  }

  @Test
  public void testToString()  {
    ResultExtractor extractor = new ValueExtractorResultExtractor(1, ProcedureCaller.DEFAULT_FETCH_SIZE);
    assertEquals("ValueExtractorResultExtractor[methodParameterIndex=1, fetchSize=default]", extractor.toString());

    extractor = new ValueExtractorResultExtractor(1, 10);
    assertEquals("ValueExtractorResultExtractor[methodParameterIndex=1, fetchSize=10]", extractor.toString());
  }

  interface SampleInterface {

    List<String> extractString(ValueExtractor<String> valueExtractor);

  }

}
