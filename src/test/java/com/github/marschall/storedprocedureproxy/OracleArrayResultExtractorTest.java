package com.github.marschall.storedprocedureproxy;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class OracleArrayResultExtractorTest {

  @Test
  public void testToString()  {
    ResultExtractor extractor = new OracleArrayResultExtractor(Integer.class);
    assertEquals("OracleArrayResultExtractor[Integer]", extractor.toString());
  }

}
