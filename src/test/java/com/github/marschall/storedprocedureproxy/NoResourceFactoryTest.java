package com.github.marschall.storedprocedureproxy;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class NoResourceFactoryTest {

  @Test
  public void testToString() {
    CallResourceFactory factory = NoResourceFactory.INSTANCE;
    assertEquals("NoResourceFactory", factory.toString());
  }

}
