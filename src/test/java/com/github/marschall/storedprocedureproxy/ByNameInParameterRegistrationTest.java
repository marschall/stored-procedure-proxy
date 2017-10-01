package com.github.marschall.storedprocedureproxy;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class ByNameInParameterRegistrationTest {

  @Test
  public void testToString() {
    InParameterRegistration registration = new ByNameInParameterRegistration(new String[] {"dog", "cat"});
    assertEquals("ByNameInParameterRegistration[dog, cat]", registration.toString());
  }

}
