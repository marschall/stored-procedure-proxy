package com.github.marschall.storedprocedureproxy;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class VoidResultExtractorTest {

  @Test
  public void testToString()  {
    ResultExtractor extractor = VoidResultExtractor.INSTANCE;
    assertEquals("VoidResultExtractor", extractor.toString());
  }

}
