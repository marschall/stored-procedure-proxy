package com.github.marschall.storedprocedureproxy;

import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.SQLException;
import java.sql.Types;

import com.github.marschall.storedprocedureproxy.ProcedureCallerFactory.ProcedureCaller;

/**
 * Strategy on how to register out parameters.
 */
interface InParameterRegistration {

  void bindInParamters(CallableStatement statement, CallResource callResource, Object[] args) throws SQLException;

  static boolean hasScale(int type) {
    return type == Types.NUMERIC || type == Types.DECIMAL;
  }

}

/**
 * No in parameters are registered.
 */
final class NoInParameterRegistration implements InParameterRegistration {

  static final InParameterRegistration INSTANCE = new NoInParameterRegistration();

  private NoInParameterRegistration() {
    super();
  }

  @Override
  public void bindInParamters(CallableStatement statement, CallResource callResource, Object[] args) {
    // nothing
  }

  @Override
  public String toString() {
    return this.getClass().getSimpleName();
  }

}

/**
 * In parameters are registered by index.
 */
final class ByIndexInParameterRegistration implements InParameterRegistration {

  // an interface method can not have more than 254 parameters
  private final byte[] inParameterIndices;

  ByIndexInParameterRegistration(byte[] inParameterIndices) {
    this.inParameterIndices = inParameterIndices;
  }

  private int inParameterIndexAt(int i) {
    return ByteUtils.toByte(this.inParameterIndices[i]);
  }

  @Override
  public void bindInParamters(CallableStatement statement, CallResource callResource, Object[] args) throws SQLException {
    for (int i = 0; i < args.length; i++) {
      int parameterIndex = this.inParameterIndexAt(i);
      if (parameterIndex == ProcedureCaller.NO_IN_PARAMTER) {
        // -> is a value extractor
        continue;
      }
      Object arg;
      if (callResource.hasResourceAt(i)) {
        arg = callResource.resourceAt(i);
      } else {
        arg = args[i];
      }
      statement.setObject(parameterIndex, arg);
    }
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append(this.getClass().getSimpleName());
    builder.append('[');
    ByteUtils.toStringOn(this.inParameterIndices, builder);
    builder.append(']');
    return builder.toString();
  }

}

/**
 * In parameters are registered by name.
 */
final class ByNameInParameterRegistration implements InParameterRegistration {

  private final String[] inParameterNames;

  ByNameInParameterRegistration(String[] inParameterNames) {
    this.inParameterNames = inParameterNames;
  }

  @Override
  public void bindInParamters(CallableStatement statement, CallResource callResource, Object[] args) throws SQLException {
    for (int i = 0; i < args.length; i++) {
      String parameterName = this.inParameterNames[i];
      if (parameterName == null) {
        // -> is a value extractor
        continue;
      }
      // REVIEW null check?
      Object arg;
      if (callResource.hasResourceAt(i)) {
        arg = callResource.resourceAt(i);
      } else {
        arg = args[i];
      }
      statement.setObject(parameterName, arg);
    }
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append(this.getClass().getSimpleName());
    builder.append('[');
    ToStringUtils.toStringOn(this.inParameterNames, builder);
    builder.append(']');
    return builder.toString();
  }

}

/**
 * In parameters are registered by index and type.
 */
final class ByIndexAndTypeInParameterRegistration implements InParameterRegistration {

  // an interface method can not have more than 254 parameters
  private final byte[] inParameterIndices;
  private final int[] inParameterTypes;

  ByIndexAndTypeInParameterRegistration(byte[] inParameterIndices, int[] inParameterTypes) {
    this.inParameterIndices = inParameterIndices;
    this.inParameterTypes = inParameterTypes;
  }

  private int inParameterIndexAt(int i) {
    return ByteUtils.toByte(this.inParameterIndices[i]);
  }

  @Override
  public void bindInParamters(CallableStatement statement, CallResource callResource, Object[] args) throws SQLException {
    for (int i = 0; i < args.length; i++) {
      int parameterIndex = this.inParameterIndexAt(i);
      if (parameterIndex == ProcedureCaller.NO_IN_PARAMTER) {
        // -> is a value extractor
        continue;
      }
      Object arg;
      if (callResource.hasResourceAt(i)) {
        arg = callResource.resourceAt(i);
      } else {
        arg = args[i];
      }
      int type = this.inParameterTypes[i];
      if (arg != null) {
        if (InParameterRegistration.hasScale(type) && arg instanceof BigDecimal) {
          // if we don't do this a scale of 0 is assumed
          statement.setObject(parameterIndex, arg, type, ((BigDecimal) arg).scale());
        } else {
          // Javadoc
          // This method is similar to #setObject
          // except that it assumes a scale of zero!
          statement.setObject(parameterIndex, arg, type);
        }
      } else {
        statement.setNull(parameterIndex, type);
      }
    }
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append(this.getClass().getSimpleName());
    builder.append("[indexes={");
    ByteUtils.toStringOn(this.inParameterIndices, builder);
    builder.append("}, types={");
    ToStringUtils.toStringOn(this.inParameterTypes, builder);
    builder.append("}]");
    return builder.toString();
  }

}

/**
 * In parameters are registered by name and type.
 */
final class ByNameAndTypeInParameterRegistration implements InParameterRegistration {

  private final String[] inParameterNames;
  private final int[] inParameterTypes;

  ByNameAndTypeInParameterRegistration(String[] inParameterNames, int[] inParameterTypes) {
    this.inParameterTypes = inParameterTypes;
    this.inParameterNames = inParameterNames;
  }

  @Override
  public void bindInParamters(CallableStatement statement, CallResource callResource, Object[] args) throws SQLException {
    for (int i = 0; i < args.length; i++) {
      String parameterName = this.inParameterNames[i];
      if (parameterName == null) {
        // -> is a value extractor
        continue;
      }
      Object arg;
      if (callResource.hasResourceAt(i)) {
        arg = callResource.resourceAt(i);
      } else {
        arg = args[i];
      }
      int type = this.inParameterTypes[i];
      if (arg != null) {
        if (InParameterRegistration.hasScale(type) && arg instanceof BigDecimal) {
          // if we don't do this a scale of 0 is assumed
          statement.setObject(parameterName, arg, type, ((BigDecimal) arg).scale());
        } else {
          // Javadoc
          // This method is similar to #setObject
          // except that it assumes a scale of zero!
          statement.setObject(parameterName, arg, type);
        }
      } else {
        statement.setNull(parameterName, type);
      }
    }
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append(this.getClass().getSimpleName());
    builder.append("[names={");
    ToStringUtils.toStringOn(this.inParameterNames, builder);
    builder.append("}, types={");
    ToStringUtils.toStringOn(this.inParameterTypes, builder);
    builder.append("}]");
    return builder.toString();
  }

}

