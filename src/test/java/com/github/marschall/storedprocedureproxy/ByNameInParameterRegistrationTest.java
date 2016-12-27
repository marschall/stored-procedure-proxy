package com.github.marschall.storedprocedureproxy;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ByNameInParameterRegistrationTest {

  @Test
  public void testToString() {
    InParameterRegistration registration = new ByNameInParameterRegistration(new String[] {"dog", "cat"});
    assertEquals("ByNameInParameterRegistration[dog, cat]", registration.toString());
  }

}
