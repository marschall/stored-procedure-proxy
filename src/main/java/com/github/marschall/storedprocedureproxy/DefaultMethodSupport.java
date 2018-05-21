package com.github.marschall.storedprocedureproxy;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;

interface DefaultMethodSupport {

  MethodHandle getDefaultMethodHandle(Object proxy, Method method);

}
