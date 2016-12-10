package com.github.marschall.storedprocedureproxy;

import java.sql.Array;
import java.sql.Connection;
import java.sql.SQLException;

interface CallResource extends AutoCloseable {

  void initialize(Connection connection) throws SQLException;

  void close() throws SQLException;

}

enum NoResource implements CallResource {

  INSTANCE;

  @Override
  public void initialize(Connection connection) {
    // empty
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
  public void initialize(Connection connection) throws SQLException {
    for (CallResource resource : this.resources) {
      resource.initialize(connection);
    }
  }

  @Override
  public void close() throws SQLException {
    for (CallResource resource : this.resources) {
      resource.close();
    }
  }

}

final class ArrayResource implements CallResource {

  private final Object[] elements;
  private final String typeName;
  private Array array;

  ArrayResource(Object[] elements, String typeName) {
    this.elements = elements;
    this.typeName = typeName;
  }

  @Override
  public void initialize(Connection connection) throws SQLException {
    this.array = connection.createArrayOf(this.typeName, this.elements);

  }

  @Override
  public void close() throws SQLException {
    this.array.free();
  }

}