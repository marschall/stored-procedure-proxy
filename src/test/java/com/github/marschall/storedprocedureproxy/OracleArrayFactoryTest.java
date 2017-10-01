package com.github.marschall.storedprocedureproxy;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class OracleArrayFactoryTest {

  @Test
  public void testToString() {
    CallResourceFactory factory = new OracleArrayFactory(1, "INTEGER");
    assertEquals("OracleArrayFactory[argumentIndex=1, typeName=INTEGER]", factory.toString());
  }

}
