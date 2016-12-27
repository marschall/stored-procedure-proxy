package com.github.marschall.storedprocedureproxy;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class VoidResultExtractorTest {

  @Test
  public void testToString()  {
    ResultExtractor extractor = VoidResultExtractor.INSTANCE;
    assertEquals("VoidResultExtractor", extractor.toString());
  }

}
