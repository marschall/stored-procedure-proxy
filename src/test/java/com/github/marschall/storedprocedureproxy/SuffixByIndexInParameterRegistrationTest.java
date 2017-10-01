package com.github.marschall.storedprocedureproxy;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class SuffixByIndexInParameterRegistrationTest {

  @Test
  public void testToString() {
    InParameterRegistration registration = SuffixByIndexInParameterRegistration.INSTANCE;
    assertEquals("SuffixByIndexInParameterRegistration[i + 1]", registration.toString());
  }

}
