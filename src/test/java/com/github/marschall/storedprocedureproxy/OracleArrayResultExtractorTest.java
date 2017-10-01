package com.github.marschall.storedprocedureproxy;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class OracleArrayResultExtractorTest {

  @Test
  public void testToString()  {
    ResultExtractor extractor = new OracleArrayResultExtractor(Integer.class);
    assertEquals("OracleArrayResultExtractor[Integer]", extractor.toString());
  }

}
