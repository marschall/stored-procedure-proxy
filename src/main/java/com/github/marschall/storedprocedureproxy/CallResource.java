package com.github.marschall.storedprocedureproxy;

import java.sql.Array;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;

interface CallResource extends AutoCloseable {

  boolean hasResourceAt(int index);

  Object resourceAt(int index);

  void close() throws SQLException;

}

enum NoResource implements CallResource {

  INSTANCE;

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
    for (CallResource resource : this.resources) {
      resource.close();
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

enum NoResourceFactory implements CallResourceFactory {

  INSTANCE;

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
    throw new IllegalArgumentException("argument at index: " + this.argumentIndex + " expected to be a collection or array but was not");
  }

}