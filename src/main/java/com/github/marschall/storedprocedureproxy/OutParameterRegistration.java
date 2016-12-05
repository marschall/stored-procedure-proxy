package com.github.marschall.storedprocedureproxy;

import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;

/**
 * Strategy on how to register out parameters.
 */
interface OutParameterRegistration {

  void bindOutParamter(CallableStatement statement) throws SQLException;

  <T> T getOutParamter(CallableStatement statement, Class<T> type) throws SQLException;

}


/**
 * Register out parameters by index and type.
 */
final class ByIndexOutParameterRegistration implements OutParameterRegistration {

  // an interface method can not have more than 254 parameters
  private final int outParameterIndex;
  private final int outParameterType;

  ByIndexOutParameterRegistration(int outParameterIndex, int outParameterType) {
    this.outParameterIndex = outParameterIndex;
    this.outParameterType = outParameterType;
  }

  @Override
  public void bindOutParamter(CallableStatement statement) throws SQLException {
    statement.registerOutParameter(outParameterIndex, outParameterType);
  }

  @Override
  public <T> T getOutParamter(CallableStatement statement, Class<T> type) throws SQLException {
    try {
      return statement.getObject(this.outParameterIndex, type);
    } catch (SQLFeatureNotSupportedException e) {
      // Postgres hack
      return type.cast(statement.getObject(this.outParameterIndex));
    }
  }

}

/**
 * Register out parameters by name and type only.
 */
final class ByNameOutParameterRegistration implements OutParameterRegistration {

  private final int outParameterType;
  private final String outParameterName;

  ByNameOutParameterRegistration(String outParameterName, int outParameterType) {
    this.outParameterType = outParameterType;
    this.outParameterName = outParameterName;
  }

  @Override
  public void bindOutParamter(CallableStatement statement) throws SQLException {
    statement.registerOutParameter(outParameterName, outParameterType);
  }

  @Override
  public <T> T getOutParamter(CallableStatement statement, Class<T> type) throws SQLException {
    try {
      return statement.getObject(this.outParameterName, type);
    } catch (SQLFeatureNotSupportedException e) {
      // Postgres hack
      return type.cast(statement.getObject(this.outParameterName));
    }
  }

}

/**
 * No out parameters are registered. Either because the procedure
 * doesn't return any results or returns the result by means of a
 * {@link ResultSet}.
 */
enum NoOutParameterRegistration implements OutParameterRegistration {

  INSTANCE;

  @Override
  public void bindOutParamter(CallableStatement statement) {
    // nothing
  }

  @Override
  public <T> T getOutParamter(CallableStatement statement, Class<T> type) throws SQLException {
    if (type != void.class) {
      throw new IllegalArgumentException("no out parameter registered");
    }
    return null;
  }

}