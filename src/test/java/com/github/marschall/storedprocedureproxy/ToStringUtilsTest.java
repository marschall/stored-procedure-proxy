package com.github.marschall.storedprocedureproxy;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.annotation.Annotation;

import org.junit.jupiter.api.Test;

import com.github.marschall.storedprocedureproxy.ProcedureCallerFactory.ProcedureCaller;

public class ToStringUtilsTest {

  @Test
  public void fetchSizeToString() {
    assertEquals("default", ToStringUtils.fetchSizeToString(ProcedureCaller.DEFAULT_FETCH_SIZE));
    assertEquals("1", ToStringUtils.fetchSizeToString(1));
  }

  @Test
  public void classNameToString() {
    assertEquals("String", ToStringUtils.classNameToString(java.lang.String.class));
    assertEquals("java.lang.annotation.Annotation", ToStringUtils.classNameToString(Annotation.class));
    assertEquals("com.github.marschall.storedprocedureproxy.ToStringUtilsTest", ToStringUtils.classNameToString(ToStringUtilsTest.class));
  }

}
