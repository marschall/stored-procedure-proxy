package com.github.marschall.storedprocedureproxy;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.github.marschall.storedprocedureproxy.ProcedureCallerFactory.ParameterRegistration;

final class Java9DefaultMethodSupport implements DefaultMethodSupport {

  private static final MethodHandle CLASS_GETMODULE;

  private static final MethodHandle MODULE_ISNAMED;

  private static final MethodHandle MODULE_ADDREADS;

  private static final MethodHandle PRIVE_LOOKUP_IN;

  static {
    MethodHandle classGetModule;
    MethodHandle moduleIsNamed;
    MethodHandle moduleAddReads;
    MethodHandle privateLookupIn;
    try {
      Class<?> moduleClass = Class.forName("java.lang.Module");

      // java.lang.Class.getModule()
      MethodType returnsModule = MethodType.methodType(moduleClass);
      classGetModule = MethodHandles.publicLookup().findVirtual(Class.class, "getModule", returnsModule);

      // java.lang.Module.isNamed()
      MethodType returnsBoolean = MethodType.methodType(boolean.class);
      moduleIsNamed = MethodHandles.publicLookup().findVirtual(moduleClass, "isNamed", returnsBoolean );

      // java.lang.Module.addReads(Module)
      MethodType addReadsSignature = MethodType.methodType(moduleClass, moduleClass);
      moduleAddReads = MethodHandles.lookup().findVirtual(moduleClass, "addReads", addReadsSignature)
              .bindTo(classGetModule.invoke(ParameterRegistration.class));

      // java.lang.invoke.MethodHandles.privateLookupIn(Class<?>, Lookup)
      MethodType privateLookupInSignature = MethodType.methodType(Lookup.class, Class.class, Lookup.class);
      privateLookupIn = MethodHandles.lookup().findStatic(MethodHandles.class, "privateLookupIn", privateLookupInSignature);
    } catch (RuntimeException e) {
      throw e;
    } catch (ReflectiveOperationException e) {
      throw new RuntimeException("could not initialize class", e);
    } catch (Error e) {
      throw e;
    } catch (Throwable e) {
      throw new RuntimeException("could not initialize class", e);
    }
    CLASS_GETMODULE = classGetModule;
    MODULE_ISNAMED = moduleIsNamed;
    MODULE_ADDREADS = moduleAddReads;
    PRIVE_LOOKUP_IN = privateLookupIn;
  }

  private final Class<?> interfaceDeclaration;

  /**
   * We assume this is uncontended since we only do a few lookups and gets.
   * Save the memory overhead of a {@link ConcurrentHashMap}.
   */
  private final Map<Method, MethodHandle> defaultMethodCache;

  private final ReadWriteLock cacheLock;

  Java9DefaultMethodSupport(Class<?> interfaceDeclaration) {
    this.interfaceDeclaration = interfaceDeclaration;
    this.defaultMethodCache = new HashMap<>();
    this.cacheLock = new ReentrantReadWriteLock();
  }

  @Override
  public MethodHandle getDefaultMethodHandle(Object proxy, Method method) {
    MethodHandle methodHandle = this.getDefaultMethodHandleFromCacheOrNull(method);
    if (methodHandle != null) {
      return methodHandle;
    }

    // potentially compute callInfo multiple times
    // rather than locking for a long time
    methodHandle = this.lookupDefaultMethod(proxy, method);

    MethodHandle previous = this.tryWriteDefaultMethodHandleToCache(method, methodHandle);
    return previous != null ? previous : methodHandle;
  }

  private MethodHandle getDefaultMethodHandleFromCacheOrNull(Method method) {
    Lock lock = this.cacheLock.readLock();
    lock.lock();
    try {
      return this.defaultMethodCache.get(method);
    } finally {
      lock.unlock();
    }
  }

  private MethodHandle tryWriteDefaultMethodHandleToCache(Method method, MethodHandle methodHandle) {
    Lock lock = this.cacheLock.writeLock();
    lock.lock();
    try {
      return this.defaultMethodCache.putIfAbsent(method, methodHandle);
    } finally {
      lock.unlock();
    }
  }

  private MethodHandle lookupDefaultMethod(Object proxy, Method method) {
    // https://gist.github.com/raphw/c1faf2f40e80afce6f13511098cfb90f
    try {
      Lookup lookup;
      try {
        // proxy.getClass().getModule().isNamed()
        if ((boolean) MODULE_ISNAMED.invoke(CLASS_GETMODULE.invoke(proxy.getClass()))) {
          lookup = (Lookup) PRIVE_LOOKUP_IN.invoke(this.interfaceDeclaration, MethodHandles.lookup());
        } else {
          // ProcedureCaller.class.getModule().addReads(proxy.getClass().getModule());
          MODULE_ADDREADS.invoke(CLASS_GETMODULE.invoke(proxy.getClass()));
          lookup = (Lookup) PRIVE_LOOKUP_IN.invoke(proxy.getClass(), MethodHandles.lookup());
        }
      } catch (RuntimeException e) {
        throw e;
      } catch (Error e) {
        throw e;
      } catch (Throwable e) {
        throw new RuntimeException("create lookup for default method", e);
      }
      MethodType methodType = MethodType.methodType(method.getReturnType(), method.getParameterTypes());
      return lookup.findSpecial(this.interfaceDeclaration, method.getName(), methodType, this.interfaceDeclaration)
              .bindTo(proxy);
    } catch (ReflectiveOperationException e) {
      throw new IllegalArgumentException("default method " + method + " is not accessible", e);
    }
  }

}
