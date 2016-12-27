package com.github.marschall.storedprocedureproxy;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class NoResourceTest {

  @Test
  public void testToString() {
    CallResource resource = NoResource.INSTANCE;
    assertEquals("NoResource", resource.toString());
  }

}
