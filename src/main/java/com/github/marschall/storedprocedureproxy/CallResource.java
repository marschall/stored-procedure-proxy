package com.github.marschall.storedprocedureproxy;

import java.sql.Array;
import java.sql.Connection;
import java.sql.SQLException;

interface CallResource extends AutoCloseable {

  void close() throws SQLException;

}

enum NoResource implements CallResource {

  INSTANCE;

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
  public void close() throws SQLException {
    for (CallResource resource : this.resources) {
      resource.close();
    }
  }

}


interface CallResourceFactory {

  CallResource createResource(Connection connection) throws SQLException;

}

enum NoResourceFactory implements CallResourceFactory {

  INSTANCE;

  @Override
  public CallResource createResource(Connection connection) throws SQLException {
    return NoResource.INSTANCE;
  }

}

final class CompositeFactory implements CallResourceFactory {

  private final CallResourceFactory[] factories;

  CompositeFactory(CallResourceFactory[] factories) {
    this.factories = factories;
  }

  @Override
  public CallResource createResource(Connection connection) throws SQLException {
    CallResource[] resources = new CallResource[this.factories.length];
    for (int i = 0; i < this.factories.length; ++i) {
      CallResourceFactory factory = this.factories[i];
      CallResource resource = factory.createResource(connection);
      resources[i] = resource;
    }
    return new CompositeResource(resources);
  }

}

final class ArrayFactory implements CallResourceFactory {

  private final Object[] elements;
  private final String typeName;

  ArrayFactory(Object[] elements, String typeName) {
    this.elements = elements;
    this.typeName = typeName;
  }



  @Override
  public CallResource createResource(Connection connection) throws SQLException {
    Array array = connection.createArrayOf(this.typeName, this.elements);
    return new ArrayResource(array);
  }

}

final class ArrayResource implements CallResource {

  private final Array array;

  ArrayResource(Array array) {
    this.array = array;
  }

  @Override
  public void close() throws SQLException {
    this.array.free();
  }

}