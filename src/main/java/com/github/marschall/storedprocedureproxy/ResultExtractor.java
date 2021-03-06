package com.github.marschall.storedprocedureproxy;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.sql.Array;
import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import com.github.marschall.storedprocedureproxy.ProcedureCallerFactory.ProcedureCaller;

/**
 * Extracts the result of a stored procedure call.
 */
interface ResultExtractor {

  /**
   *
   * @param statement the executed statement
   * @param outParameterRegistration the out parameter registration
   * @param args the method arguments, may contain a {@link NumberedValueExtractor} or {@link NumberedValueExtractor}.
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
      if (count > 1) {
        throw new SQLException("expected at most 1 rows but got " + count);
      }
    }
    return null;
  }

  @Override
  public String toString() {
    return "VoidResultExtractor";
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
      return this.readFromResultSet(statement, outParameterRegistration);
    } else {
      return this.readFromStatement(statement, outParameterRegistration);
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

  @Override
  public String toString() {
    return this.getClass().getSimpleName() +'[' + ToStringUtils.classNameToString(this.returnType) + ']';
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
    if (this.fetchSize != ProcedureCaller.DEFAULT_FETCH_SIZE) {
      statement.setFetchSize(this.fetchSize);
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

  @Override
  public String toString() {
    return this.getClass().getSimpleName() +"[type=" + ToStringUtils.classNameToString(this.listElementType)
      + ", fetchSize=" + ToStringUtils.fetchSizeToString(this.fetchSize) + ']';
  }

}

abstract class AbstractValueExtractorResultExtractor implements ResultExtractor {

  private final int extractorIndex;

  private final int fetchSize;

  AbstractValueExtractorResultExtractor(int extractorIndex, int fetchSize) {
    this.extractorIndex = extractorIndex;
    this.fetchSize = fetchSize;
  }

  @Override
  public Object extractResult(CallableStatement statement, OutParameterRegistration outParameterRegistration, Object[] args) throws SQLException {
    if (this.fetchSize != ProcedureCaller.DEFAULT_FETCH_SIZE) {
      statement.setFetchSize(this.fetchSize);
    }
    Object extractor = args[this.extractorIndex];
    boolean hasResultSet = statement.execute();
    if (hasResultSet) {
      try (ResultSet rs = statement.getResultSet()) {
        return this.read(rs, extractor);
      }
    } else {
      try (ResultSet rs = getOutResultSet(statement, outParameterRegistration)) {
        return this.read(rs, extractor);
      }
    }
  }

  abstract Object read(ResultSet rs, Object extractor) throws SQLException;

  private static ResultSet getOutResultSet(CallableStatement statement, OutParameterRegistration outParameterRegistration) throws SQLException {
    return outParameterRegistration.getOutParamter(statement, ResultSet.class);
  }

  @Override
  public String toString() {
    return this.getClass().getSimpleName() +"[methodParameterIndex=" + this.extractorIndex
            + ", fetchSize=" + ToStringUtils.fetchSizeToString(this.fetchSize) + ']';
  }

}

/**
 * Extracts a {@link List} using a {@link NumberedValueExtractor}.
 */
final class NumberedValueExtractorResultExtractor extends AbstractValueExtractorResultExtractor {

  NumberedValueExtractorResultExtractor(int extractorIndex, int fetchSize) {
    super(extractorIndex, fetchSize);
  }

  @Override
  Object read(ResultSet rs, Object extractor) throws SQLException {
    return read(rs, (NumberedValueExtractor<?>) extractor);
  }

  private static List<Object> read(ResultSet resultSet, NumberedValueExtractor<?> valueExtractor) throws SQLException {
    List<Object> result = new ArrayList<>();
    int rowNumber = 0;
    while (resultSet.next()) {
      Object element = valueExtractor.extractValue(resultSet, rowNumber);
      result.add(element);
      rowNumber += 1;
    }
    return result;
  }

}

/**
 * Extracts a {@link List} using a {@link ValueExtractor}.
 */
final class ValueExtractorResultExtractor extends AbstractValueExtractorResultExtractor {

  ValueExtractorResultExtractor(int extractorIndex, int fetchSize) {
    super(extractorIndex, fetchSize);
  }

  @Override
  Object read(ResultSet rs, Object extractor) throws SQLException {
    return read(rs, (ValueExtractor<?>) extractor);
  }

  private static List<Object> read(ResultSet resultSet, ValueExtractor<?> valueExtractor) throws SQLException {
    List<Object> result = new ArrayList<>();
    while (resultSet.next()) {
      Object element = valueExtractor.extractValue(resultSet);
      result.add(element);
    }
    return result;
  }

}

/**
 * Extracts a {@link List} using a {@link Function}.
 */
final class FunctionResultExtractor extends AbstractValueExtractorResultExtractor {

  FunctionResultExtractor(int extractorIndex, int fetchSize) {
    super(extractorIndex, fetchSize);
  }

  @Override
  @SuppressWarnings("unchecked")
  Object read(ResultSet rs, Object extractor) throws SQLException {
    return read(rs, (Function<ResultSet, ?>) extractor);
  }

  private static List<Object> read(ResultSet resultSet, Function<ResultSet, ?> function) throws SQLException {
    List<Object> result = new ArrayList<>();
    while (resultSet.next()) {
      Object element = function.apply(resultSet);
      result.add(element);
    }
    return result;
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
        Array jdbcArray = rs.getArray(1);
        return this.extractValue(jdbcArray);
      }
    } else {
      Array array = outParameterRegistration.getOutParamter(statement, Array.class);
      return this.extractValue(array);
    }
  }

  private Object extractValue(Array jdbcArray) throws SQLException {
    try {
      Object array = jdbcArray.getArray();
      Class<? extends Object> arrayClass = array.getClass();
      if (!arrayClass.isArray()) {
        throw new ClassCastException("expected array of " + this.arrayElementType + " but got " + arrayClass);
      }
      Class<?> actualComponentType = arrayClass.getComponentType();
      if (actualComponentType == this.arrayElementType) {
        return array;
      }
      return this.convertElementType(array, actualComponentType);
    } finally {
      jdbcArray.free();
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

  @Override
  public String toString() {
    return this.getClass().getSimpleName() +'[' + ToStringUtils.classNameToString(this.arrayElementType) + ']';
  }
}

final class OracleArrayResultExtractor implements ResultExtractor {

  private static final Class<?> ORACLE_ARRAY;
  private static final MethodHandle GET_LONG_ARRAY;
  private static final MethodHandle GET_INT_ARRAY;
  private static final MethodHandle GET_DOUBLE_ARRAY;
  private static final MethodHandle GET_FLOAT_ARRAY;
  private static final MethodHandle GET_SHORT_ARRAY;

  static {
    // https://docs.oracle.com/en/database/oracle/oracle-database/12.2/jajdb/oracle/jdbc/OracleArray.html
    Class<?> oracleArray;
    MethodHandle getLongArray;
    MethodHandle getIntArray;
    MethodHandle getFloatArray;
    MethodHandle getDoubleArray;
    MethodHandle getShortArray;
    try {
      oracleArray = Class.forName("oracle.jdbc.OracleArray");

      Lookup lookup = MethodHandles.publicLookup();
      getLongArray = lookup.unreflect(oracleArray.getDeclaredMethod("getLongArray"));
      getIntArray = lookup.unreflect(oracleArray.getDeclaredMethod("getIntArray"));
      getFloatArray = lookup.unreflect(oracleArray.getDeclaredMethod("getFloatArray"));
      getDoubleArray = lookup.unreflect(oracleArray.getDeclaredMethod("getDoubleArray"));
      getShortArray = lookup.unreflect(oracleArray.getDeclaredMethod("getShortArray"));
    } catch (ReflectiveOperationException e) {
      oracleArray = null;

      getLongArray = null;
      getIntArray = null;
      getFloatArray = null;
      getDoubleArray = null;
      getShortArray = null;
    }
    ORACLE_ARRAY = oracleArray;
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
    if (ORACLE_ARRAY == null) {
      throw new IllegalStateException("Oracle JDBC classes not available");
    }
    boolean hasResultSet = statement.execute();
    if (hasResultSet) {
      try (ResultSet rs = statement.getResultSet()) {
        if (!rs.next()) {
          throw new IllegalStateException("result set is empty");
        }
        Array array = (Array) rs.getObject(1, ORACLE_ARRAY);
        return this.extractValue(array);
      }
    } else {
      Array array = (Array) outParameterRegistration.getOutParamter(statement, ORACLE_ARRAY);
      return this.extractValue(array);
    }
  }

  static boolean isSupportedElementType(Class<?> elementType) {
    return elementType.isPrimitive()
            && ((elementType == int.class)
                    || (elementType == long.class)
                    || (elementType == float.class)
                    || (elementType == double.class)
                    || (elementType == short.class));

  }

  private Object extractValue(Array array) throws SQLException {
    try {
      if (this.arrayElementType.isPrimitive()) {
        if (this.arrayElementType == int.class) {
          return getIntArray(array);
        } else if (this.arrayElementType == long.class) {
          return getLongArray(array);
        } else if (this.arrayElementType == float.class) {
          return getFloatArray(array);
        } else if (this.arrayElementType == double.class) {
          return getDoubleArray(array);
        } else if (this.arrayElementType == short.class) {
          return getShortArray(array);
        } else {
          throw new IllegalArgumentException("unsupported element type: " + this.arrayElementType);
        }
      } else {
        throw new IllegalStateException("for reference arrays " + ArrayResultExtractor.class + " should be used");
      }
    } finally {
      array.free();
    }
  }

  private static Object getShortArray(Array array) {
    // short[]
    try {
      return GET_SHORT_ARRAY.invoke(array);
    } catch (RuntimeException e) {
      throw e;
    } catch (Error e) {
      throw e;
    } catch (Throwable e) {
      // should not happen, does not fall into type signature
      throw new RuntimeException("unknwon exception occured when calling " + GET_SHORT_ARRAY, e);
    }
  }

  private static Object getDoubleArray(Array array) {
    // double[]
    try {
      return GET_DOUBLE_ARRAY.invoke(array);
    } catch (RuntimeException e) {
      throw e;
    } catch (Error e) {
      throw e;
    } catch (Throwable e) {
      // should not happen, does not fall into type signature
      throw new RuntimeException("unknwon exception occured when calling " + GET_DOUBLE_ARRAY, e);
    }
  }

  private static Object getFloatArray(Array array) {
    // float[]
    try {
      return GET_FLOAT_ARRAY.invoke(array);
    } catch (RuntimeException e) {
      throw e;
    } catch (Error e) {
      throw e;
    } catch (Throwable e) {
      // should not happen, does not fall into type signature
      throw new RuntimeException("unknwon exception occured when calling " + GET_FLOAT_ARRAY, e);
    }
  }

  private static Object getLongArray(Array array) {
    // long[]
    try {
      return GET_LONG_ARRAY.invoke(array);
    } catch (RuntimeException e) {
      throw e;
    } catch (Error e) {
      throw e;
    } catch (Throwable e) {
      // should not happen, does not fall into type signature
      throw new RuntimeException("unknwon exception occured when calling " + GET_LONG_ARRAY, e);
    }
  }

  private static Object getIntArray(Array array) {
    // int[]
    try {
      return GET_INT_ARRAY.invoke(array);
    } catch (RuntimeException e) {
      throw e;
    } catch (Error e) {
      throw e;
    } catch (Throwable e) {
      // should not happen, does not fall into type signature
      throw new RuntimeException("unknwon exception occured when calling " + GET_INT_ARRAY, e);
    }
  }

  @Override
  public String toString() {
    return this.getClass().getSimpleName() +'[' + ToStringUtils.classNameToString(this.arrayElementType) + ']';
  }

}

@FunctionalInterface
interface ArrayResultExtractorFactory {

  ResultExtractor newArrayResultExtractor(Class<?> methodReturnType);

  ArrayResultExtractorFactory JDBC = (methodReturnType) -> new ArrayResultExtractor(methodReturnType.getComponentType());

  ArrayResultExtractorFactory ORACLE = (methodReturnType) -> {
    Class<?> componentType = methodReturnType.getComponentType();
    if (OracleArrayResultExtractor.isSupportedElementType(componentType)) {
      return new OracleArrayResultExtractor(componentType);
    } else {
      return new ArrayResultExtractor(componentType);
    }
  };

}

