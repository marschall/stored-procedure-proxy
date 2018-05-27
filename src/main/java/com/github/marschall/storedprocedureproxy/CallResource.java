package com.github.marschall.storedprocedureproxy;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.sql.Array;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;

import org.postgresql.PGConnection;

interface CallResource extends AutoCloseable {

  boolean hasResourceAt(int index);

  Object resourceAt(int index);

  @Override
  void close() throws SQLException;

}

final class NoResource implements CallResource {

  static final CallResource INSTANCE = new NoResource();

  private NoResource() {
    super();
  }

  @Override
  public boolean hasResourceAt(int index) {
    return false;
  }

  @Override
  public Object resourceAt(int index) {
    throw new IllegalArgumentException("no resource at: " + index);
  }

  @Override
  public void close() {
    // empty
  }

  @Override
  public String toString() {
    return this.getClass().getSimpleName();
  }

}

final class CompositeResource implements CallResource {

  private final CallResource[] resources;

  CompositeResource(CallResource[] resources) {
    this.resources = resources;
  }

  @Override
  public boolean hasResourceAt(int index) {
    for (CallResource resource : this.resources) {
      if (resource.hasResourceAt(index)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public Object resourceAt(int index) {
    // we do not expect to have many call resources so a linear scan
    // should be fine
    for (CallResource resource : this.resources) {
      if (resource.hasResourceAt(index)) {
        return resource.resourceAt(index);
      }
    }
    throw new IllegalArgumentException("no resource at: " + index);
  }

  @Override
  public void close() throws SQLException {
    SQLException firstException = null;
    for (CallResource resource : this.resources) {
      try {
        resource.close();
      } catch (SQLException e) {
        if (firstException == null) {
          firstException = e;
        } else {
          firstException.addSuppressed(e);
        }
      }
    }
    if (firstException != null) {
      throw firstException;
    }
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append(this.getClass().getSimpleName());
    builder.append('[');
    ToStringUtils.toStringOn(this.resources, builder);
    builder.append(']');
    return builder.toString();
  }

}

final class ArrayResource implements CallResource {

  private final Array array;
  private final int arrayIndex;

  ArrayResource(Array array, int arrayIndex) {
    this.array = array;
    this.arrayIndex = arrayIndex;
  }

  @Override
  public void close() throws SQLException {
    this.array.free();
  }

  @Override
  public boolean hasResourceAt(int index) {
    return index == this.arrayIndex;
  }

  @Override
  public Object resourceAt(int index) {
    if (index != this.arrayIndex) {
      throw new IllegalArgumentException("no resource at: " + index);
    }
    return this.array;
  }

  @Override
  public String toString() {
    return this.getClass().getSimpleName() + '[' + this.arrayIndex + ']';
  }

}

interface CallResourceFactory {

  CallResource createResource(Connection connection, Object[] args) throws SQLException;

}

final class NoResourceFactory implements CallResourceFactory {

  static final CallResourceFactory INSTANCE = new NoResourceFactory();

  private NoResourceFactory() {
    super();
  }

  @Override
  public CallResource createResource(Connection connection, Object[] args) {
    return NoResource.INSTANCE;
  }

  @Override
  public String toString() {
    return this.getClass().getSimpleName();
  }

}

final class CompositeFactory implements CallResourceFactory {

  private final CallResourceFactory[] factories;

  CompositeFactory(CallResourceFactory[] factories) {
    this.factories = factories;
  }

  @Override
  public CallResource createResource(Connection connection, Object[] args) throws SQLException {
    CallResource[] resources = new CallResource[this.factories.length];
    for (int i = 0; i < this.factories.length; ++i) {
      CallResourceFactory factory = this.factories[i];
      CallResource resource;
      try {
        resource = factory.createResource(connection, args);
      } catch (SQLException e) {
        cleanUp(resources, i, e);
        throw e;
      }
      resources[i] = resource;
    }
    return new CompositeResource(resources);
  }

  private static void cleanUp(CallResource[] resources, int endIndex, SQLException originalException) {
    for (int i = 0; i < endIndex; ++i) {
      CallResource resource = resources[i];
      try {
        resource.close();
      } catch (SQLException e) {
        originalException.addSuppressed(e);
      }
    }
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append(this.getClass().getSimpleName());
    builder.append('[');
    ToStringUtils.toStringOn(this.factories, builder);
    builder.append(']');
    return builder.toString();
  }

}

abstract class AbstractArrayFactory implements CallResourceFactory {

  final int argumentIndex;
  final String typeName;

  AbstractArrayFactory(int argumentIndex, String typeName) {
    this.argumentIndex = argumentIndex;
    this.typeName = typeName;
  }

  CallResource createArrayOf(Connection connection, Object[] args) throws SQLException {
    // REVIEW what if null
    Object[] elements = this.extractElements(args);
    Array array = connection.createArrayOf(this.typeName, elements);
    return new ArrayResource(array, this.argumentIndex);
  }

  private Object[] extractElements(Object[] args) {
    Object elements = args[this.argumentIndex];
    if (elements instanceof Collection) {
      return ((Collection<?>) elements).toArray();
    }
    if (elements instanceof Object[]) {
      return (Object[]) elements;
    }
    if (elements.getClass().isArray()) {
      // primitive array
      int length = java.lang.reflect.Array.getLength(elements);
      Object[] array = new Object[length];
      for (int i = 0; i < length; ++i) {
        array[i] = java.lang.reflect.Array.get(elements, i);
      }
      return array;
    }
    throw new IllegalArgumentException("argument at index: " + this.argumentIndex + " expected to be a collection or array but was not");
  }

  @Override
  public String toString() {
    return this.getClass().getSimpleName() + "[argumentIndex=" + this.argumentIndex
            + ", typeName=" + this.typeName + ']';
  }

}

final class ArrayFactory extends AbstractArrayFactory {

  ArrayFactory(int argumentIndex, String typeName) {
    super(argumentIndex, typeName);
  }

  @Override
  public CallResource createResource(Connection connection, Object[] args) throws SQLException {
    return this.createArrayOf(connection, args);
  }

}

/**
 * Creates array using the PostgreS API. PostgreS supports creating
 * arrays from primitive arrays.
 */
final class PgArrayFactory extends AbstractArrayFactory {

  PgArrayFactory(int argumentIndex, String typeName) {
    super(argumentIndex, typeName);
  }

  @Override
  public CallResource createResource(Connection connection, Object[] args)
          throws SQLException {

    Object elements = args[this.argumentIndex];
    if ((elements != null) && isSupportedPrimitiveArray(elements.getClass())) {
      return this.createPgArrayOf(connection, elements);
    } else {
      return this.createArrayOf(connection, args);
    }
  }

  private ArrayResource createPgArrayOf(Connection connection, Object elements) throws SQLException {
    PGConnection pgConnection = connection.unwrap(PGConnection.class);
    Array array = pgConnection.createArrayOf(this.typeName, elements);
    return new ArrayResource(array, this.argumentIndex);
  }

  private static boolean isSupportedPrimitiveArray(Class<?> clazz) {
    // org.postgresql.jdbc.PrimitiveArraySupport.isSupportedPrimitiveArray(Object)
    return (clazz == long[].class)
            || (clazz == int[].class)
            || (clazz == short[].class)
            || (clazz == double[].class)
            || (clazz == float[].class)
            || (clazz == boolean[].class)
            || (clazz == String[].class);
  }

}

/**
 * Creates array using the Oracle API. In Oracle arrays are created
 * using the array name instead of the element name. In addition
 * Oracle supports creating arrays from primitive arrays.
 */
final class OracleArrayFactory extends AbstractArrayFactory {

  private static final Class<?> ORACLE_CONNECTION;
  private static final MethodHandle CREATE_ORACLE_ARRAY;

  static {
    // https://docs.oracle.com/database/121/JJDBC/oraarr.htm#JJDBC28574
    // https://docs.oracle.com/en/database/oracle/oracle-database/12.2/jajdb/oracle/jdbc/OracleConnection.html#createOracleArray-java.lang.String-java.lang.Object-
    Class<?> oracleConnection;
    MethodHandle createARRAY;
    try {
      oracleConnection = Class.forName("oracle.jdbc.OracleConnection");
      Method createOracleArrayMethod = oracleConnection.getDeclaredMethod(
              "createOracleArray", String.class, Object.class);
      createARRAY = MethodHandles.publicLookup().unreflect(createOracleArrayMethod);
    } catch (ReflectiveOperationException e) {
      oracleConnection = null;
      createARRAY = null;
    }
    ORACLE_CONNECTION = oracleConnection;
    CREATE_ORACLE_ARRAY = createARRAY;
  }

  OracleArrayFactory(int argumentIndex, String typeName) {
    super(argumentIndex, typeName);
  }

  @Override
  public CallResource createResource(Connection connection, Object[] args) throws SQLException {
    if ((ORACLE_CONNECTION == null) || (CREATE_ORACLE_ARRAY == null)) {
      throw new IllegalStateException("Oracle JDBC classes not found in expected shape");
    }
    // REVIEW what if null
    Object elements = this.extractElements(args);
    Array array = this.createOracleArray(elements, connection);
    return new ArrayResource(array, this.argumentIndex);
  }

  private Array createOracleArray(Object elements, Connection connection) throws SQLException {
    Object oracleConnection = connection.unwrap(ORACLE_CONNECTION);
    Array array;
    try {
      array = (Array) CREATE_ORACLE_ARRAY.invoke(oracleConnection, this.typeName, elements);
    } catch (SQLException e) {
      throw e;
    } catch (RuntimeException e) {
      throw e;
    } catch (Error e) {
      throw e;
    } catch (Throwable e) {
      // should not happen, does not fall into type signature
      throw new RuntimeException("unknwon exception occured when calling " + CREATE_ORACLE_ARRAY, e);
    }
    return array;
  }

  private Object extractElements(Object[] args) {
    Object elements = args[this.argumentIndex];
    if (elements instanceof Collection) {
      return ((Collection<?>) elements).toArray();
    }
    if (elements instanceof Object[]) {
      return elements;
    }
    if (elements.getClass().isArray()) {
      // primitive array, directly supported by Oracle
      return elements;
    }
    throw new IllegalArgumentException("argument at index: " + this.argumentIndex + " expected to be a collection or array but was not");
  }

}

interface ArrayResourceFactoryFactory {

  CallResourceFactory createArrayFactory(int argumentIndex, String typeName);

  ArrayResourceFactoryFactory JDBC = ArrayFactory::new;

  ArrayResourceFactoryFactory ORACLE = OracleArrayFactory::new;

  ArrayResourceFactoryFactory POSTGRES = PgArrayFactory::new;

}

