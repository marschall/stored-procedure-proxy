package com.github.marschall.storedprocedureproxy;

import static org.junit.Assert.assertEquals;

import java.sql.Types;

import org.junit.Test;

public class ByIndexOutParameterRegistrationTest {

  @Test
  public void byteIntConversions() {
    assertInt(0);
    assertInt(127);
    assertInt(128);
    assertInt(254);
  }

  private static void assertInt(int i) {
    assertEquals(i, ByIndexOutParameterRegistration.toInt(ByIndexOutParameterRegistration.toByte(i)));
  }

  @Test
  public void testToString() {
    OutParameterRegistration registration = new ByIndexOutParameterRegistration(254, Types.INTEGER);
    assertEquals("ByIndexOutParameterRegistration[index=254, type=4]", registration.toString());
  }

}
