package com.github.marschall.storedprocedureproxy;

public final class Travis {

  private Travis() {
    throw new AssertionError("not instantiable");
  }

  public static boolean isTravis() {
    return System.getenv().getOrDefault("TRAVIS", "false").equals("true");
  }

}
