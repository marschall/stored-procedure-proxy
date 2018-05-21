package com.github.marschall.storedprocedureproxy;

final class DefaultMethodSupportFactory {

  private static final boolean SUPPORTS_DEFAULT_METHODS = isJava9OrLater();

  static DefaultMethodSupport newInstance(Class<?> interfaceDeclaration) {
    if (SUPPORTS_DEFAULT_METHODS) {
      return new Java9DefaultMethodSupport(interfaceDeclaration);
    } else {
      return NoDefaultMethodSupport.INSTANCE;
    }
  }

  private static boolean isJava9OrLater() {
    try {
      Class.forName("java.lang.Runtime$Version");
      return true;
    } catch (ClassNotFoundException e) {
      return false;
    }
  }

}
