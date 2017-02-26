package com.github.marschall.storedprocedureproxy;

import static org.junit.Assert.assertEquals;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;

import org.junit.Test;

public class MethodHandleTest {

  private static final MethodHandle CREATE_ARRAY;

  static {
    try {
      Method method = OracleInterface.class.getDeclaredMethod("createArray", String.class, Object.class);
      CREATE_ARRAY = MethodHandles.publicLookup().unreflect(method);
    } catch (ReflectiveOperationException e) {
      throw new ExceptionInInitializerError(e);
    }
  }

  @Test
  public void createArray() throws Throwable {
    Object oracle = new OracleImplementation();

    String s = (String) CREATE_ARRAY.invoke(oracle, "string", (Object) 1);
    assertEquals("s: string, o: 1", s);
  }

  public interface OracleInterface {

    String createArray(String s, Object o);

    int[] getIntArray();

  }

  static class OracleImplementation implements OracleInterface {

    @Override
    public String createArray(String s, Object o) {
      return "s: " + s + ", o: " + o;
    }

    @Override
    public int[] getIntArray() {
      return new int[] {1, 2, 3};
    }

  }

}
