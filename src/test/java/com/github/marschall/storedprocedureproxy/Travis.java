package com.github.marschall.storedprocedureproxy;

import org.aopalliance.aop.AspectException;

public final class Travis {

  private Travis() {
    throw new AspectException("not instantiable");
  }

  public static boolean isTravis() {
    return System.getenv().getOrDefault("TRAVIS", "false").equals("true");
  }

}
