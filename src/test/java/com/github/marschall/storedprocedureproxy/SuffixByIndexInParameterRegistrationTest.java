package com.github.marschall.storedprocedureproxy;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class SuffixByIndexInParameterRegistrationTest {

  @Test
  public void testToString() {
    InParameterRegistration registration = SuffixByIndexInParameterRegistration.INSTANCE;
    assertEquals("SuffixByIndexInParameterRegistration[i + i]", registration.toString());
  }

}
