package com.github.marschall.storedprocedureproxy;

import java.sql.Array;
import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.github.marschall.storedprocedureproxy.ProcedureCallerFactory.ProcedureCaller;

/**
 * Extracts the result of a stored procedure call.
 */
interface ResultExtractor {

  /**
   *
   * @param statement the executed statement
   * @param outParameterRegistration the out parameter registration
   * @param args the method arguments, may contain a {@link ValueExtractor}.
   * @return the value of the procedure call
   * @throws SQLException if the JDBC driver throws an exception
   */
  Object extractResult(CallableStatement statement, OutParameterRegistration outParameterRegistration, Object[] args) throws SQLException;

}

/**
 * Extracts nothing and returns {@code null}.
 */
enum VoidResultExtractor implements ResultExtractor {

  INSTANCE;

  @Override
  public Object extractResult(CallableStatement statement, OutParameterRegistration outParameterRegistration, Object[] args) throws SQLException {
    boolean hasResultSet = statement.execute();
    if (hasResultSet) {
      int count = 0;
      try (ResultSet rs = statement.executeQuery()) {
        while (rs.next()) {
          count += 1;
        }
      }
      // don't check count H2 just returns NULL
    }
    return null;
  }

}

/**
 * Extracts a single value.
 */
final class ScalarResultExtractor implements ResultExtractor {

  /**
   * Instead of {@code int.class} contains {@code Integer.class}.
   */
  private final Class<?> returnType;

  ScalarResultExtractor(Class<?> returnType) {
    this.returnType = returnType;
  }

  @Override
  public Object extractResult(CallableStatement statement, OutParameterRegistration outParameterRegistration, Object[] args) throws SQLException {
    // REVIEW for functions does retrieving the value by name make sense?
    boolean hasResultSet = statement.execute();
    if (hasResultSet) {
      return readFromResultSet(statement, outParameterRegistration);
    } else {
      return readFromStatement(statement, outParameterRegistration);
    }
  }

  private Object readFromStatement(CallableStatement statement, OutParameterRegistration outParameterRegistration) throws SQLException {
    return outParameterRegistration.getOutParamter(statement, this.returnType);
  }

  private Object readFromResultSet(CallableStatement statement, OutParameterRegistration outParameterRegistration) throws SQLException {
    Object last = null;
    int count = 0;
    try (ResultSet rs = statement.getResultSet()) {
      while (rs.next()) {
         last = rs.getObject(1, this.returnType);
      }
      count += 1;
    }
    if (count != 1) {
      ProcedureCallerFactory.newIncorrectResultSizeException(1, count);
    }
    return this.returnType.cast(last);
  }

}

/**
 * Extracts a {@link List} of scalar values.
 */
final class ListResultExtractor implements ResultExtractor {

  private final Class<?> listElementType;

  private final int fetchSize;

  ListResultExtractor(Class<?> listElementType, int fetchSize) {
    this.listElementType = listElementType;
    this.fetchSize = fetchSize;
  }

  @Override
  public Object extractResult(CallableStatement statement, OutParameterRegistration outParameterRegistration, Object[] args) throws SQLException {
    if (fetchSize != ProcedureCaller.DEFAULT_FETCH_SIZE) {
      statement.setFetchSize(fetchSize);
    }
    boolean hasResultSet = statement.execute();
    if (hasResultSet) {
      try (ResultSet rs = statement.getResultSet()) {
        return read(rs, this.listElementType);
      }
    } else {
      try (ResultSet rs = getOutResultSet(statement, outParameterRegistration)) {
        return read(rs, this.listElementType);
      }
    }
  }

  private static List<Object> read(ResultSet resultSet, Class<?> type) throws SQLException {
    List<Object> result = new ArrayList<>();
    while (resultSet.next()) {
      Object element = resultSet.getObject(1, type);
      result.add(element);
    }
    return result;
  }

  private static ResultSet getOutResultSet(CallableStatement statement, OutParameterRegistration outParameterRegistration) throws SQLException {
    return outParameterRegistration.getOutParamter(statement, ResultSet.class);
  }

}

/**
 * Extracts a {@link List} using a {@link ValueExtractor}.
 */
final class ValueExtractorResultExtractor implements ResultExtractor {

  private final int extractorIndex;

  private final int fetchSize;

  ValueExtractorResultExtractor(int extractorIndex, int fetchSize) {
    this.extractorIndex = extractorIndex;
    this.fetchSize = fetchSize;
  }

  @Override
  public Object extractResult(CallableStatement statement, OutParameterRegistration outParameterRegistration, Object[] args) throws SQLException {
    if (fetchSize != ProcedureCaller.DEFAULT_FETCH_SIZE) {
      statement.setFetchSize(fetchSize);
    }
    ValueExtractor<?> valueExtractor = (ValueExtractor<?>) args[this.extractorIndex];
    boolean hasResultSet = statement.execute();
    if (hasResultSet) {
      try (ResultSet rs = statement.getResultSet()) {
        return read(rs, valueExtractor);
      }
    } else {
      try (ResultSet rs = getOutResultSet(statement, outParameterRegistration)) {
        return read(rs, valueExtractor);
      }
    }
  }

  private static List<Object> read(ResultSet resultSet, ValueExtractor<?> valueExtractor) throws SQLException {
    List<Object> result = new ArrayList<>();
    int rowNumber = 0;
    while (resultSet.next()) {
      Object element = valueExtractor.extractValue(resultSet, rowNumber);
      result.add(element);
      rowNumber += 1;
    }
    return result;
  }

  private static ResultSet getOutResultSet(CallableStatement statement, OutParameterRegistration outParameterRegistration) throws SQLException {
    return outParameterRegistration.getOutParamter(statement, ResultSet.class);
  }

}


/**
 * Extracts a {@link Array} of scalar values.
 */
final class ArrayResultExtractor implements ResultExtractor {

  private final Class<?> arrayElementType;

  ArrayResultExtractor(Class<?> arrayElementType) {
    this.arrayElementType = arrayElementType;
  }

  @Override
  public Object extractResult(CallableStatement statement, OutParameterRegistration outParameterRegistration, Object[] args) throws SQLException {
    boolean hasResultSet = statement.execute();
    if (hasResultSet) {
      try (ResultSet rs = statement.getResultSet()) {
        rs.next();
        Array array = rs.getArray(1);
        return extractValue(array);
      }
    } else {
      Array array = outParameterRegistration.getOutParamter(statement, Array.class);
      return extractValue(array);
    }
  }

  private Object extractValue(Array array) throws SQLException {
    try {
      Object value = array.getArray();
      Class<? extends Object> clazz = value.getClass();
      if (!clazz.isArray()) {
        throw new ClassCastException("expected array of " + this.arrayElementType + " but got " + clazz);
      }
      Class<?> actualComponentType = clazz.getComponentType();
      if (actualComponentType == this.arrayElementType) {
        return value;
      }
      return convertElementType(value, actualComponentType);
    } finally {
      array.free();
    }
  }

  private Object convertElementType(Object array, Class<?> actualComponentType) {
    if (actualComponentType.isPrimitive() == this.arrayElementType.isPrimitive()) {
      throw new ClassCastException("expected array of " + this.arrayElementType + " but got array of " + actualComponentType);
    }
    int length = java.lang.reflect.Array.getLength(array);
    Object value = java.lang.reflect.Array.newInstance(this.arrayElementType, length);
    for (int i = 0; i < length; ++i) {
      Object elementValue = java.lang.reflect.Array.get(array, i);
      java.lang.reflect.Array.set(value, i, elementValue);
    }
    return value;
  }
}

