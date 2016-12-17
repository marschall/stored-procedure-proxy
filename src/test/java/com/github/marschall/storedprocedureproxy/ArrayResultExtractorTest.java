package com.github.marschall.storedprocedureproxy;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.sql.Array;
import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.junit.Test;

public class ArrayResultExtractorTest {

  private static final Object[] NO_ARGS = new Object[0];

  @Test
  public void referenceToPrimitiveNoResultSet() throws SQLException {
    // given
    ResultExtractor extractor = new ArrayResultExtractor(int.class);
    CallableStatement statement = mock(CallableStatement.class);
    Array array = mock(Array.class);
    OutParameterRegistration outParameterRegistration = mock(OutParameterRegistration.class);

    when(statement.execute()).thenReturn(false);
    when(outParameterRegistration.getOutParamter(statement, Array.class)).thenReturn(array);
    when(array.getArray()).thenReturn(new Integer[] {1});

    // when
    Object result = extractor.extractResult(statement, outParameterRegistration, NO_ARGS);

    // then
    assertNotNull(result);
    assertTrue(result.getClass().isArray());
    assertEquals(int.class, result.getClass().getComponentType());
    assertArrayEquals(new int[] {1}, (int[]) result);
  }

  @Test
  public void referenceToReferenceNoResultSet() throws SQLException {
    // given
    ResultExtractor extractor = new ArrayResultExtractor(Integer.class);
    CallableStatement statement = mock(CallableStatement.class);
    Array array = mock(Array.class);
    OutParameterRegistration outParameterRegistration = mock(OutParameterRegistration.class);

    when(statement.execute()).thenReturn(false);
    when(outParameterRegistration.getOutParamter(statement, Array.class)).thenReturn(array);
    when(array.getArray()).thenReturn(new Integer[] {1});

    // when
    Object result = extractor.extractResult(statement, outParameterRegistration, NO_ARGS);

    // then
    assertNotNull(result);
    assertTrue(result.getClass().isArray());
    assertEquals(Integer.class, result.getClass().getComponentType());
    assertArrayEquals(new Integer[] {1}, (Integer[]) result);
  }

  @Test
  public void referenceToPrimitiveResultSet() throws SQLException {
    // given
    ResultExtractor extractor = new ArrayResultExtractor(int.class);
    CallableStatement statement = mock(CallableStatement.class);
    ResultSet resultSet = mock(ResultSet.class);
    Array array = mock(Array.class);
    OutParameterRegistration outParameterRegistration = mock(OutParameterRegistration.class);

    when(statement.execute()).thenReturn(true);
    when(statement.getResultSet()).thenReturn(resultSet);
    when(resultSet.getArray(1)).thenReturn(array);
    when(array.getArray()).thenReturn(new Integer[] {1});

    // when
    Object result = extractor.extractResult(statement, outParameterRegistration, NO_ARGS);

    // then
    assertNotNull(result);
    assertTrue(result.getClass().isArray());
    assertEquals(int.class, result.getClass().getComponentType());
    assertArrayEquals(new int[] {1}, (int[]) result);
  }

  @Test
  public void referenceToReferenceResultSet() throws SQLException {
    // given
    ResultExtractor extractor = new ArrayResultExtractor(Integer.class);
    CallableStatement statement = mock(CallableStatement.class);
    ResultSet resultSet = mock(ResultSet.class);
    Array array = mock(Array.class);
    OutParameterRegistration outParameterRegistration = mock(OutParameterRegistration.class);

    when(statement.execute()).thenReturn(true);
    when(statement.getResultSet()).thenReturn(resultSet);
    when(resultSet.getArray(1)).thenReturn(array);
    when(array.getArray()).thenReturn(new Integer[] {1});

    // when
    Object result = extractor.extractResult(statement, outParameterRegistration, NO_ARGS);

    // then
    assertNotNull(result);
    assertTrue(result.getClass().isArray());
    assertEquals(Integer.class, result.getClass().getComponentType());
    assertArrayEquals(new Integer[] {1}, (Integer[]) result);
  }

  @Test
  public void primitiveToPrimitiveNoResultSet() throws SQLException {
    // given
    ResultExtractor extractor = new ArrayResultExtractor(int.class);
    CallableStatement statement = mock(CallableStatement.class);
    Array array = mock(Array.class);
    OutParameterRegistration outParameterRegistration = mock(OutParameterRegistration.class);

    when(statement.execute()).thenReturn(false);
    when(outParameterRegistration.getOutParamter(statement, Array.class)).thenReturn(array);
    when(array.getArray()).thenReturn(new int[] {1});

    // when
    Object result = extractor.extractResult(statement, outParameterRegistration, NO_ARGS);

    // then
    assertNotNull(result);
    assertTrue(result.getClass().isArray());
    assertEquals(int.class, result.getClass().getComponentType());
    assertArrayEquals(new int[] {1}, (int[]) result);
  }

  @Test
  public void primitiveToReferenceNoResultSet() throws SQLException {
    // given
    ResultExtractor extractor = new ArrayResultExtractor(Integer.class);
    CallableStatement statement = mock(CallableStatement.class);
    Array array = mock(Array.class);
    OutParameterRegistration outParameterRegistration = mock(OutParameterRegistration.class);

    when(statement.execute()).thenReturn(false);
    when(outParameterRegistration.getOutParamter(statement, Array.class)).thenReturn(array);
    when(array.getArray()).thenReturn(new int[] {1});

    // when
    Object result = extractor.extractResult(statement, outParameterRegistration, NO_ARGS);

    // then
    assertNotNull(result);
    assertTrue(result.getClass().isArray());
    assertEquals(Integer.class, result.getClass().getComponentType());
    assertArrayEquals(new Integer[] {1}, (Integer[]) result);
  }

  @Test
  public void primitiveToPrimitiveResultSet() throws SQLException {
    // given
    ResultExtractor extractor = new ArrayResultExtractor(int.class);
    CallableStatement statement = mock(CallableStatement.class);
    ResultSet resultSet = mock(ResultSet.class);
    Array array = mock(Array.class);
    OutParameterRegistration outParameterRegistration = mock(OutParameterRegistration.class);

    when(statement.execute()).thenReturn(true);
    when(statement.getResultSet()).thenReturn(resultSet);
    when(resultSet.getArray(1)).thenReturn(array);
    when(array.getArray()).thenReturn(new int[] {1});

    // when
    Object result = extractor.extractResult(statement, outParameterRegistration, NO_ARGS);

    // then
    assertNotNull(result);
    assertTrue(result.getClass().isArray());
    assertEquals(int.class, result.getClass().getComponentType());
    assertArrayEquals(new int[] {1}, (int[]) result);
  }

  @Test
  public void primitiveToReferenceResultSet() throws SQLException {
    // given
    ResultExtractor extractor = new ArrayResultExtractor(Integer.class);
    CallableStatement statement = mock(CallableStatement.class);
    ResultSet resultSet = mock(ResultSet.class);
    Array array = mock(Array.class);
    OutParameterRegistration outParameterRegistration = mock(OutParameterRegistration.class);

    when(statement.execute()).thenReturn(true);
    when(statement.getResultSet()).thenReturn(resultSet);
    when(resultSet.getArray(1)).thenReturn(array);
    when(array.getArray()).thenReturn(new int[] {1});

    // when
    Object result = extractor.extractResult(statement, outParameterRegistration, NO_ARGS);

    // then
    assertNotNull(result);
    assertTrue(result.getClass().isArray());
    assertEquals(Integer.class, result.getClass().getComponentType());
    assertArrayEquals(new Integer[] {1}, (Integer[]) result);
  }

}
