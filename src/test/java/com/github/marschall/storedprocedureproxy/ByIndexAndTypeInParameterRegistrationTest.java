package com.github.marschall.storedprocedureproxy;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.sql.Types;

import org.junit.jupiter.api.Test;

public class ByIndexAndTypeInParameterRegistrationTest {

  @Test
  public void testToString() {
    InParameterRegistration registration = new ByIndexAndTypeInParameterRegistration(
            new byte[] {1, -128, -1}, new int[] {Types.VARCHAR, Types.INTEGER, Types.TIMESTAMP});
    assertEquals("ByIndexAndTypeInParameterRegistration[indexes={1, 128, 255}, types={12, 4, 93}]", registration.toString());
  }

}
