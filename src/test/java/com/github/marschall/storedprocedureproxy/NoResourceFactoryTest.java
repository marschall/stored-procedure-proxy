package com.github.marschall.storedprocedureproxy;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class NoResourceFactoryTest {

  @Test
  public void testToString() {
    CallResourceFactory factory = NoResourceFactory.INSTANCE;
    assertEquals("NoResourceFactory", factory.toString());
  }

}
