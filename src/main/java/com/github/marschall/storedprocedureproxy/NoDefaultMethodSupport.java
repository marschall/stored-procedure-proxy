package com.github.marschall.storedprocedureproxy;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;

final class NoDefaultMethodSupport implements DefaultMethodSupport {

  static final DefaultMethodSupport INSTANCE = new NoDefaultMethodSupport();

  @Override
  public MethodHandle getDefaultMethodHandle(Object proxy, Method method) {
    throw new IllegalStateException("default methods are not only supported in Java 9 or later");
  }

}
