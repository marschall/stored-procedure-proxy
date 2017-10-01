package com.github.marschall.storedprocedureproxy;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class PrefixByIndexInParameterRegistrationTest {

  @Test
  public void testToString() {
    InParameterRegistration registration = PrefixByIndexInParameterRegistration.INSTANCE;
    assertEquals("PrefixByIndexInParameterRegistration[i + 2]", registration.toString());
  }

}
