package com.github.marschall.storedprocedureproxy;

final class JavaVersionSupport {

  private JavaVersionSupport() {
    throw new AssertionError("not instantiable");
  }

  static boolean isJava9OrLater() {
    try {
      Class.forName("java.lang.Runtime$Version");
      return true;
    } catch (ClassNotFoundException e) {
      return false;
    }
  }

}
