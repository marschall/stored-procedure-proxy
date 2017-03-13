package com.github.marschall.storedprocedureproxy;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class PrefixByIndexInParameterRegistrationTest {

  @Test
  public void testToString() {
    InParameterRegistration registration = PrefixByIndexInParameterRegistration.INSTANCE;
    assertEquals("PrefixByIndexInParameterRegistration[i + 2]", registration.toString());
  }

}
