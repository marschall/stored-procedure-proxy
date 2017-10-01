package com.github.marschall.storedprocedureproxy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
    assertThrows(IllegalArgumentException.class, () -> ByteUtils.toByte(-1));

    assertThrows(IllegalArgumentException.class, () -> ByteUtils.toByte(256));
  }

  private static void assertInt(int i) {
    assertEquals(i, ByteUtils.toInt(ByteUtils.toByte(i)));
  }

}
