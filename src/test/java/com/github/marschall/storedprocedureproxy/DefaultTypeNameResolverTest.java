package com.github.marschall.storedprocedureproxy;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Collection;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.github.marschall.storedprocedureproxy.spi.TypeNameResolver;

public class DefaultTypeNameResolverTest {

  private TypeNameResolver typeNameResolver;

  @Before
  public void setUp() {
    this.typeNameResolver = DefaultTypeNameResolver.INSTANCE;
  }

  @Test
  public void getTypeName() throws ReflectiveOperationException {
    Method method = SampleInterface.class.getDeclaredMethod("sampleMethod", List.class, Collection.class, Integer[].class, int[].class);
    Parameter[] parameters = method.getParameters();

    assertEquals("VARCHAR", this.typeNameResolver.getTypeName(parameters[0]));
    assertEquals("BIGINT", this.typeNameResolver.getTypeName(parameters[1]));
    assertEquals("INTEGER", this.typeNameResolver.getTypeName(parameters[2]));
    assertEquals("INTEGER", this.typeNameResolver.getTypeName(parameters[3]));
  }

  interface SampleInterface {


    void sampleMethod(List<String> stringList, Collection<Long> longCollection, Integer[] referenceArray, int[] primitveArray);

  }

}
