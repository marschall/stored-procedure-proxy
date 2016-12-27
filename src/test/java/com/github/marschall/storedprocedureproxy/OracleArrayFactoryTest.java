package com.github.marschall.storedprocedureproxy;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class OracleArrayFactoryTest {

  @Test
  public void testToString() {
    CallResourceFactory factory = new OracleArrayFactory(1, "INTEGER");
    assertEquals("OracleArrayFactory[argumentIndex=1, typeName=INTEGER]", factory.toString());
  }

}
