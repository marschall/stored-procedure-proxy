package com.github.marschall.storedprocedureproxy;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class NoResourceTest {

  @Test
  public void testToString() {
    CallResource resource = NoResource.INSTANCE;
    assertEquals("NoResource", resource.toString());
  }

}
