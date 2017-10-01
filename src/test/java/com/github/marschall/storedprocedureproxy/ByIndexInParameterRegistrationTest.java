package com.github.marschall.storedprocedureproxy;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class ByIndexInParameterRegistrationTest {

  @Test
  public void testToString() {
    InParameterRegistration registration = new ByIndexInParameterRegistration(new byte[] {1, -128, -1});
    assertEquals("ByIndexInParameterRegistration[1, 128, 255]", registration.toString());
  }

}
