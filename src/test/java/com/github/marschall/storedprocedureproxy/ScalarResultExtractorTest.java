package com.github.marschall.storedprocedureproxy;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class ScalarResultExtractorTest {

  @Test
  public void testToString()  {
    ResultExtractor extractor = new ScalarResultExtractor(Integer.class);
    assertEquals("ScalarResultExtractor[Integer]", extractor.toString());
  }

}
