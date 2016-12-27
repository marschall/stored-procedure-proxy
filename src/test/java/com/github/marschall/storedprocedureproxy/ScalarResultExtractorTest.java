package com.github.marschall.storedprocedureproxy;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ScalarResultExtractorTest {

  @Test
  public void testToString()  {
    ResultExtractor extractor = new ScalarResultExtractor(Integer.class);
    assertEquals("ScalarResultExtractor[Integer]", extractor.toString());
  }

}
