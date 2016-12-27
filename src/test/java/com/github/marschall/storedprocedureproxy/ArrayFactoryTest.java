package com.github.marschall.storedprocedureproxy;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ArrayFactoryTest {

  @Test
  public void testToString() {
    CallResourceFactory factory = new ArrayFactory(1, "INTEGER");
    assertEquals("ArrayFactory[argumentIndex=1, typeName=INTEGER]", factory.toString());
  }

}
