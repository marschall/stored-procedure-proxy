package com.github.marschall.storedprocedureproxy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import java.sql.Array;
import java.sql.SQLException;

import org.junit.jupiter.api.Test;

public class ArrayResourceTest {

  @Test
  public void testToString() throws SQLException {
    Array array = mock(Array.class);
    try (CallResource resource = new ArrayResource(array, 1)) {
      assertEquals("ArrayResource[1]", resource.toString());
    }
  }

}
