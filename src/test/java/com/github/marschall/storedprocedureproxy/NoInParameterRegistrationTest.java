package com.github.marschall.storedprocedureproxy;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class NoInParameterRegistrationTest {

  @Test
  public void testToString() {
    InParameterRegistration registration = NoInParameterRegistration.INSTANCE;
    assertEquals("NoInParameterRegistration", registration.toString());
  }

}
