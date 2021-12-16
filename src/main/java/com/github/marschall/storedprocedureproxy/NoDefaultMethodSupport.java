package com.github.marschall.storedprocedureproxy;

import java.lang.reflect.Method;

final class NoDefaultMethodSupport implements DefaultMethodSupport {

  static final DefaultMethodSupport INSTANCE = new NoDefaultMethodSupport();

  @Override
  public Object invokeDefaultMethod(Object proxy, Method method, Object[] args) {
    throw new IllegalStateException("default methods are not only supported in Java 9 or later");
  }

}
