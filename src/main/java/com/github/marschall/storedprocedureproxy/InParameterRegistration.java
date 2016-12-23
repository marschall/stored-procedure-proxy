package com.github.marschall.storedprocedureproxy;

import java.sql.CallableStatement;
import java.sql.SQLException;

import com.github.marschall.storedprocedureproxy.ProcedureCallerFactory.ProcedureCaller;

/**
 * Strategy on how to register out parameters.
 */
interface InParameterRegistration {

  void bindInParamters(CallableStatement statement, CallResource callResource, Object[] args) throws SQLException;

}


final class NoInParameterRegistration implements InParameterRegistration {

  static final InParameterRegistration INSTANCE = new NoInParameterRegistration();

  private NoInParameterRegistration() {
    super();
  }

  @Override
  public void bindInParamters(CallableStatement statement, CallResource callResource, Object[] args) {
    // nothing
  }

}

final class ByIndexInParameterRegistration implements InParameterRegistration {

  // an interface method can not have more than 254 parameters
  private final byte[] inParameterIndices;

  ByIndexInParameterRegistration(byte[] inParameterIndices) {
    this.inParameterIndices = inParameterIndices;
  }

  private int inParameterIndexAt(int i) {
    return Byte.toUnsignedInt(this.inParameterIndices[i]);
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

}

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

}

final class ByIndexAndTypeInParameterRegistration implements InParameterRegistration {

  // an interface method can not have more than 254 parameters
  private final byte[] inParameterIndices;
  private final int[] inParameterTypes;

  ByIndexAndTypeInParameterRegistration(byte[] inParameterIndices, int[] inParameterTypes) {
    this.inParameterIndices = inParameterIndices;
    this.inParameterTypes = inParameterTypes;
  }

  private int inParameterIndexAt(int i) {
    return Byte.toUnsignedInt(this.inParameterIndices[i]);
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
        statement.setObject(parameterIndex, arg, type);
      } else {
        statement.setNull(parameterIndex, type);
      }
    }
  }

}

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
        statement.setObject(parameterName, arg, type);
      } else {
        statement.setNull(parameterName, type);
      }
    }
  }

}

