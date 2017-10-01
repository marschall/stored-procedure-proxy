package com.github.marschall.storedprocedureproxy;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class ArrayFactoryTest {

  @Test
  public void testToString() {
    CallResourceFactory factory = new ArrayFactory(1, "INTEGER");
    assertEquals("ArrayFactory[argumentIndex=1, typeName=INTEGER]", factory.toString());
  }

}
