package com.github.marschall.storedprocedureproxy;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

final class Java16DefaultMethodSupport implements DefaultMethodSupport {

  static final DefaultMethodSupport INSTANCE = new Java16DefaultMethodSupport();

  private static final MethodHandle INVOKE_DEFAULT;

  static {
    MethodHandle invokeDefaultHandle;
    try {
      Method invokeDefaultMethod = InvocationHandler.class
        .getDeclaredMethod("invokeDefault", Object.class, Method.class, Object[].class);
      invokeDefaultHandle = MethodHandles.publicLookup().unreflect(invokeDefaultMethod);
    } catch (ReflectiveOperationException e) {
      throw new RuntimeException("could not initialize class", e);
    }
    INVOKE_DEFAULT = invokeDefaultHandle;
  }

  private Java16DefaultMethodSupport() {
    super();
  }

  @Override
  public Object invokeDefaultMethod(Object proxy, Method method, Object[] args) throws Throwable {
    return INVOKE_DEFAULT.invokeExact(proxy, method, args);
  }

}
