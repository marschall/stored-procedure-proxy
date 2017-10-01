package com.github.marschall.storedprocedureproxy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.github.marschall.storedprocedureproxy.annotations.TypeName;
import com.github.marschall.storedprocedureproxy.spi.TypeNameResolver;

public class AnnotationBasedTypeNameResolverTest {

  private TypeNameResolver resolver;

  @BeforeEach
  public void setUp() {
    this.resolver = AnnotationBasedTypeNameResolver.INSTANCE;
  }

  @Test
  public void typeNameOnType() throws ReflectiveOperationException {
    Method method = SampleInterface.class.getDeclaredMethod("typeNameOnType", List.class);
    Parameter parameter = method.getParameters()[0];
    assertEquals("typeName1", this.resolver.resolveTypeName(parameter));
  }

  @Test
  public void typeNameOnParameter() throws ReflectiveOperationException {
    Method method = SampleInterface.class.getDeclaredMethod("typeNameOnParameter", List.class);
    Parameter parameter = method.getParameters()[0];
    assertEquals("typeName2", this.resolver.resolveTypeName(parameter));
  }

  @Test
  public void typeNameOnArray() throws ReflectiveOperationException {
    Method method = SampleInterface.class.getDeclaredMethod("typeNameOnArray", Integer[].class);
    Parameter parameter = method.getParameters()[0];
    assertEquals("typeName3", this.resolver.resolveTypeName(parameter));
  }

  @Test
  public void noTypeName() throws ReflectiveOperationException {
    Method method = SampleInterface.class.getDeclaredMethod("noTypeName", Integer.class);
    Parameter parameter = method.getParameters()[0];
    assertNull(this.resolver.resolveTypeName(parameter));
  }

  interface SampleInterface {

    void typeNameOnType(@TypeName("typeName1") List<Integer> ids);

    void typeNameOnParameter(List<@TypeName("typeName2") Integer> ids);

    void typeNameOnArray(@TypeName("typeName3") Integer[] ids);

    void noTypeName(Integer id);

  }

}
