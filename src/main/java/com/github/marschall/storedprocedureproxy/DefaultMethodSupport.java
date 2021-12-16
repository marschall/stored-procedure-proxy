package com.github.marschall.storedprocedureproxy;

import java.lang.reflect.Method;

interface DefaultMethodSupport {

  Object invokeDefaultMethod(Object proxy, Method method, Object[] args) throws Throwable;

}
