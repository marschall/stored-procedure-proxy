package com.github.marschall.storedprocedureproxy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.List;

import org.junit.Test;

import com.github.marschall.storedprocedureproxy.ProcedureCallerFactory.ProcedureCaller;
import com.github.marschall.storedprocedureproxy.annotations.TypeName;

public class ProcedureCallerFactoryTest {

  @Test
  public void typeNameOnType() throws ReflectiveOperationException {
    Method method = SampleInterface.class.getDeclaredMethod("typeNameOnType", List.class);
    Parameter parameter = method.getParameters()[0];
    assertEquals("typeName1", ProcedureCaller.getTypeNameFromAnnotation(parameter));
  }

  @Test
  public void typeNameOnParameter() throws ReflectiveOperationException {
    Method method = SampleInterface.class.getDeclaredMethod("typeNameOnParameter", List.class);
    Parameter parameter = method.getParameters()[0];
    assertEquals("typeName2", ProcedureCaller.getTypeNameFromAnnotation(parameter));
  }

  @Test
  public void typeNameOnArray() throws ReflectiveOperationException {
    Method method = SampleInterface.class.getDeclaredMethod("typeNameOnArray", Integer[].class);
    Parameter parameter = method.getParameters()[0];
    assertEquals("typeName3", ProcedureCaller.getTypeNameFromAnnotation(parameter));
  }

  @Test
  public void noTypeName() throws ReflectiveOperationException {
    Method method = SampleInterface.class.getDeclaredMethod("noTypeName", Integer.class);
    Parameter parameter = method.getParameters()[0];
    assertNull(ProcedureCaller.getTypeNameFromAnnotation(parameter));
  }

  interface SampleInterface {

    void typeNameOnType(@TypeName("typeName1") List<Integer> ids);

    void typeNameOnParameter(List<@TypeName("typeName2") Integer> ids);

    void typeNameOnArray(@TypeName("typeName3") Integer[] ids);

    void noTypeName(Integer id);

  }

}
