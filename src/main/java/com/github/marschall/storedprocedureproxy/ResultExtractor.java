package com.github.marschall.storedprocedureproxy;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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
final class VoidResultExtractor implements ResultExtractor {

  static final ResultExtractor INSTANCE = new VoidResultExtractor();

  private VoidResultExtractor() {
    super();
  }

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
    if (actualComponentType != Object.class) {
      if (actualComponentType.isPrimitive() == this.arrayElementType.isPrimitive()) {
        throw new ClassCastException("expected array of " + this.arrayElementType + " but got array of " + actualComponentType);
      }
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

final class OracleArrayResultExtractor implements ResultExtractor {

  private static final Class<?> ARRAY;
  private static final Method GET_LONG_ARRAY;
  private static final Method GET_INT_ARRAY;
  private static final Method GET_DOUBLE_ARRAY;
  private static final Method GET_FLOAT_ARRAY;
  private static final Method GET_SHORT_ARRAY;

  static {
    // https://docs.oracle.com/database/121/JJDBC/oraarr.htm#JJDBC28574
    Class<?> array;
    Method getLongArray;
    Method getIntArray;
    Method getFloatArray;
    Method getDoubleArray;
    Method getShortArray;
    try {
      array = Class.forName("oracle.sql.ARRAY");

      getLongArray = array.getDeclaredMethod("getLongArray");
      getIntArray = array.getDeclaredMethod("getIntArray");
      getFloatArray = array.getDeclaredMethod("getFloatArray");
      getDoubleArray = array.getDeclaredMethod("getDoubleArray");
      getShortArray = array.getDeclaredMethod("getShortArray");
    } catch (ReflectiveOperationException e) {
      array = null;

      getLongArray = null;
      getIntArray = null;
      getFloatArray = null;
      getDoubleArray = null;
      getShortArray = null;
    }
    ARRAY = array;
    GET_LONG_ARRAY = getLongArray;
    GET_INT_ARRAY = getIntArray;
    GET_DOUBLE_ARRAY = getDoubleArray;
    GET_FLOAT_ARRAY = getFloatArray;
    GET_SHORT_ARRAY = getShortArray;
  }

  private final Class<?> arrayElementType;

  OracleArrayResultExtractor(Class<?> arrayElementType) {
    this.arrayElementType = arrayElementType;
  }

  @Override
  public Object extractResult(CallableStatement statement, OutParameterRegistration outParameterRegistration, Object[] args) throws SQLException {
    if (ARRAY == null) {
      throw new IllegalStateException("Oracle JDBC classes not available");
    }
    boolean hasResultSet = statement.execute();
    if (hasResultSet) {
      try (ResultSet rs = statement.getResultSet()) {
        rs.next();
        Array array = (Array) rs.getObject(1, ARRAY);
        return extractValue(array);
      }
    } else {
      Array array = (Array) outParameterRegistration.getOutParamter(statement, ARRAY);
      return extractValue(array);
    }
  }

  private Object extractValue(Array array) throws SQLException {
    try {
      if (this.arrayElementType.isPrimitive()) {
        Method extractionMethod;
        if (this.arrayElementType == int.class) {
          extractionMethod = GET_INT_ARRAY;
        } else if (this.arrayElementType == long.class) {
          extractionMethod = GET_LONG_ARRAY;
        } else if (this.arrayElementType == float.class) {
          extractionMethod = GET_FLOAT_ARRAY;
        } else if (this.arrayElementType == double.class) {
          extractionMethod = GET_DOUBLE_ARRAY;
        } else if (this.arrayElementType == short.class) {
          extractionMethod = GET_SHORT_ARRAY;
        } else if (this.arrayElementType == byte.class) {
          throw new IllegalArgumentException("byte[] for oracle.sql.ARRAY not yet implemented");
        } else {
          throw new IllegalArgumentException("unknown element type: " + this.arrayElementType);
        }
        try {
          return extractionMethod.invoke(extractionMethod);
        } catch (IllegalAccessException e) {
          throw new RuntimeException("not allowed to call " + extractionMethod, e);
        } catch (InvocationTargetException e) {
          Throwable cause = e.getCause();
          if (cause instanceof SQLException) {
            throw (SQLException) cause;
          }
          if (cause instanceof RuntimeException) {
            throw (RuntimeException) cause;
          }
          throw new RuntimeException("exception occured when calling " + extractionMethod, cause);
        }
      } else {
        throw new IllegalStateException("for reference arrays " + ArrayResultExtractor.class + " should be used");
      }
    } finally {
      array.free();
    }
  }

}

