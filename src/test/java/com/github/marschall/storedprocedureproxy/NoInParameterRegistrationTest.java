package com.github.marschall.storedprocedureproxy;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class NoInParameterRegistrationTest {

  @Test
  public void testToString() {
    InParameterRegistration registration = NoInParameterRegistration.INSTANCE;
    assertEquals("NoInParameterRegistration", registration.toString());
  }

}
