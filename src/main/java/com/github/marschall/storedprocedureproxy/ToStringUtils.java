package com.github.marschall.storedprocedureproxy;

import com.github.marschall.storedprocedureproxy.ProcedureCallerFactory.ProcedureCaller;

final class ToStringUtils {

  private ToStringUtils() {
    throw new AssertionError("not instantiable");
  }

  static String fetchSizeToString(int fetchSize) {
    if (fetchSize == ProcedureCaller.DEFAULT_FETCH_SIZE) {
      return "default";
    } else {
      return Integer.toString(fetchSize);
    }
  }

  static String classNameToString(Class<?> clazz) {
    if (clazz.getPackage().getName().equals("java.lang")) {
      return clazz.getSimpleName();
    } else {
      return clazz.getName();
    }
  }

}
