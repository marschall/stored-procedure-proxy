package com.github.marschall.storedprocedureproxy;

import static org.junit.Assert.assertEquals;

import java.sql.Types;

import org.junit.Test;

public class ByNameAndTypeInParameterRegistrationTest {

  @Test
  public void testToString() {
    InParameterRegistration registration = new ByNameAndTypeInParameterRegistration(
            new String[] {"dog", "cat"}, new int[] {Types.VARCHAR, Types.INTEGER});
    assertEquals("ByNameAndTypeInParameterRegistration[names={dog, cat}, types={12, 4}]", registration.toString());
  }

}
