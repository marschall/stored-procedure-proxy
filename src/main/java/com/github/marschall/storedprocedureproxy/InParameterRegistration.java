package com.github.marschall.storedprocedureproxy;

import java.sql.CallableStatement;
import java.sql.SQLException;

import com.github.marschall.storedprocedureproxy.ProcedureCallerFactory.ProcedureCaller;

/**
 * Strategy on how to register out parameters.
 */
interface InParameterRegistration {

  void bindInParamters(CallableStatement statement, Object[] args) throws SQLException;

}


enum NoInParameterRegistration implements InParameterRegistration {

  INSTANCE;

  @Override
  public void bindInParamters(CallableStatement statement, Object[] args) {
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
  public void bindInParamters(CallableStatement statement, Object[] args) throws SQLException {
    if (args == null) {
      return;
    }
    for (int i = 0; i < args.length; i++) {
      int parameterIndex = this.inParameterIndexAt(i);
      if (parameterIndex == ProcedureCaller.NO_IN_PARAMTER) {
        // -> is a value extractor
        continue;
      }
      statement.setObject(parameterIndex, args[i]);
    }
  }

}

final class ByNameInParameterRegistration implements InParameterRegistration {

  private final String[] inParameterNames;

  ByNameInParameterRegistration(String[] inParameterNames) {
    this.inParameterNames = inParameterNames;
  }

  @Override
  public void bindInParamters(CallableStatement statement, Object[] args) throws SQLException {
    if (args == null) {
      return;
    }
    for (int i = 0; i < args.length; i++) {
      String parameterName = this.inParameterNames[i];
      if (parameterName == null) {
        // -> is a value extractor
        continue;
      }
      // REVIEW null check?
      statement.setObject(parameterName, args[i]);
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
  public void bindInParamters(CallableStatement statement, Object[] args) throws SQLException {
    if (args == null) {
      return;
    }
    for (int i = 0; i < args.length; i++) {
      int parameterIndex = this.inParameterIndexAt(i);
      if (parameterIndex == ProcedureCaller.NO_IN_PARAMTER) {
        // -> is a value extractor
        continue;
      }
      Object arg = args[i];
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
  public void bindInParamters(CallableStatement statement, Object[] args) throws SQLException {
    if (args == null) {
      return;
    }
    for (int i = 0; i < args.length; i++) {
      String parameterName = this.inParameterNames[i];
      if (parameterName == null) {
        // -> is a value extractor
        continue;
      }
      Object arg = args[i];
      int type = this.inParameterTypes[i];
      if (arg != null) {
        statement.setObject(parameterName, arg, type);
      } else {
        statement.setNull(parameterName, type);
      }
    }
  }

}

/**
 * Takes the value of an in parameter from a {@link CallResource}
 * rather than from the actual method argument.
 */
final class ResourceInParameterRegistration implements InParameterRegistration {

  // an interface method can not have more than 254 parameters
  private byte[] resourceIndices;

  private InParameterRegistration delegate;

  private int resourceIndexAt(int i) {
    return Byte.toUnsignedInt(this.resourceIndices[i]);
  }

  @Override
  public void bindInParamters(CallableStatement statement, Object[] args) throws SQLException {
    if (args == null) {
      return;
    }
    for (int i = 0; i < this.resourceIndices.length; i++) {
      int resourceIndex = this.resourceIndexAt(i);
      if (resourceIndex == ProcedureCaller.NO_IN_PARAMTER) {
        // either a normal in parameter or a value extractor
        continue;
      }
      CallResource callResource = null;
      // todo access call state
    }
    this.delegate.bindInParamters(statement, args);
  }


}





