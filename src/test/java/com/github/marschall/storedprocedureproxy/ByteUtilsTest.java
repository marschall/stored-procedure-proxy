package com.github.marschall.storedprocedureproxy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Test;

public class ByteUtilsTest {

  @Test
  public void byteIntConversions() {
    assertInt(0);
    assertInt(127);
    assertInt(128);
    assertInt(254);
  }

  @Test
  public void invalidConversions() {
    try {
      ByteUtils.toByte(-1);
      fail("-1 is invalid");
    } catch (IllegalArgumentException e) {
      // should reach here
    }
    try {
      ByteUtils.toByte(256);
      fail("256 is invalid");
    } catch (IllegalArgumentException e) {
      // should reach here
    }
  }

  private static void assertInt(int i) {
    assertEquals(i, ByteUtils.toInt(ByteUtils.toByte(i)));
  }

}
