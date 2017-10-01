package com.github.marschall.storedprocedureproxy;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class NoOutParameterRegistrationTest {

  @Test
  public void testToString() {
    OutParameterRegistration registration = NoOutParameterRegistration.INSTANCE;
    assertEquals("NoOutParameterRegistration", registration.toString());
  }

}
