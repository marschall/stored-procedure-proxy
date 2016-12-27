package com.github.marschall.storedprocedureproxy;

import static org.junit.Assert.assertEquals;

import java.sql.Types;

import org.junit.Test;

public class ByNameOutParameterRegistrationTest {

  @Test
  public void testToString() {
    OutParameterRegistration registration = new ByNameOutParameterRegistration("dog", Types.INTEGER);
    assertEquals("ByNameOutParameterRegistration[name=dog, type=4]", registration.toString());
  }

}
