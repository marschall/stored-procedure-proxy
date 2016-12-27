package com.github.marschall.storedprocedureproxy;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Array;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;

interface CallResource extends AutoCloseable {

  boolean hasResourceAt(int index);

  Object resourceAt(int index);

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
      CallResource resource = factory.createResource(connection, args);
      resources[i] = resource;
    }
    return new CompositeResource(resources);
  }

}

final class ArrayFactory implements CallResourceFactory {

  private final int argumentIndex;
  private final String typeName;

  ArrayFactory(int argumentIndex, String typeName) {
    this.argumentIndex = argumentIndex;
    this.typeName = typeName;
  }

  @Override
  public CallResource createResource(Connection connection, Object[] args) throws SQLException {
    // REVIEW what if null
    Object[] elements = extractElements(args);
    Array array = connection.createArrayOf(this.typeName, elements);
    return new ArrayResource(array, this.argumentIndex);
  }

  private Object[] extractElements(Object[] args) {
    Object argument = args[this.argumentIndex];
    if (argument instanceof Collection) {
      return ((Collection<?>) argument).toArray();
    }
    if (argument instanceof Object[]) {
      return (Object[]) argument;
    }
    if (argument.getClass().isArray()) {
      // primitive array
      int length = java.lang.reflect.Array.getLength(argument);
      Object[] array = new Object[length];
      for (int i = 0; i < length; ++i) {
        array[i] = java.lang.reflect.Array.get(argument, i);
      }
      return array;
    }
    throw new IllegalArgumentException("argument at index: " + this.argumentIndex + " expected to be a collection or array but was not");
  }

}

/**
 * Creates array using the Oracle API. In Oracle arrays are created
 * using the array name instead of the element name.
 */
final class OracleArrayFactory implements CallResourceFactory {

  private static final Class<?> ORACLE_CONNECTION;
  private static final Method CREATE_ARRAY;

  static {
    // https://docs.oracle.com/database/121/JJDBC/oraarr.htm#JJDBC28574
    Class<?> oracleConnection;
    Method createARRAY;
    try {
      oracleConnection = Class.forName("oracle.jdbc.OracleConnection");
      createARRAY = oracleConnection.getDeclaredMethod("createARRAY", String.class, Object.class);
    } catch (ReflectiveOperationException e) {
      oracleConnection = null;
      createARRAY = null;
    }
    ORACLE_CONNECTION = oracleConnection;
    CREATE_ARRAY = createARRAY;
  }

  private final int argumentIndex;
  private final String typeName;

  OracleArrayFactory(int argumentIndex, String typeName) {
    this.argumentIndex = argumentIndex;
    this.typeName = typeName;
  }

  @Override
  public CallResource createResource(Connection connection, Object[] args) throws SQLException {
    if (ORACLE_CONNECTION == null || CREATE_ARRAY == null) {
      throw new IllegalStateException("Oracle JDBC classes not found in expected shape");
    }
    // REVIEW what if null
    Object elements = extractElements(args);
    Object oracleConnection = connection.unwrap(ORACLE_CONNECTION);
    Array array;
    try {
      array = (Array) CREATE_ARRAY.invoke(oracleConnection, this.typeName, elements);
    } catch (InvocationTargetException e) {
      Throwable cause = e.getCause();
      if (cause instanceof SQLException) {
        throw (SQLException) cause;
      }
      if (cause instanceof RuntimeException) {
        throw (RuntimeException) cause;
      }
      throw new RuntimeException("exception occured when calling " + CREATE_ARRAY, cause);
    } catch (ReflectiveOperationException e) {
      throw new RuntimeException("exception occured when calling " + CREATE_ARRAY, e);
    }
    return new ArrayResource(array, this.argumentIndex);
  }

  private Object extractElements(Object[] args) {
    Object argument = args[this.argumentIndex];
    if (argument instanceof Collection) {
      return ((Collection<?>) argument).toArray();
    }
    if (argument instanceof Object[]) {
      return (Object[]) argument;
    }
    if (argument.getClass().isArray()) {
      // primitive array, directly supported by Oracle
      return argument;
    }
    throw new IllegalArgumentException("argument at index: " + this.argumentIndex + " expected to be a collection or array but was not");
  }

}