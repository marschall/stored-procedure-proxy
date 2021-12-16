package com.github.marschall.storedprocedureproxy;

final class DefaultMethodSupportFactory {

  static DefaultMethodSupport newInstance(Class<?> interfaceDeclaration) {
    return Java16DefaultMethodSupport.INSTANCE;
  }

}
