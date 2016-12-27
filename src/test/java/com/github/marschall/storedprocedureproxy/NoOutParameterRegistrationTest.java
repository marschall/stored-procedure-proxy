package com.github.marschall.storedprocedureproxy;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class NoOutParameterRegistrationTest {

  @Test
  public void testToString() {
    OutParameterRegistration registration = NoOutParameterRegistration.INSTANCE;
    assertEquals("NoOutParameterRegistration", registration.toString());
  }

}
