package com.github.marschall.storedprocedureproxy;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.github.marschall.storedprocedureproxy.ProcedureCallerFactory.ProcedureCaller;

public class ValueExtractorResultExtractorTest {

  @Test
  public void testToString()  {
    ResultExtractor extractor = new ValueExtractorResultExtractor(1, ProcedureCaller.DEFAULT_FETCH_SIZE);
    assertEquals("ValueExtractorResultExtractor[methodParameterIndex=1, fetchSize=default]", extractor.toString());

    extractor = new ValueExtractorResultExtractor(1, 10);
    assertEquals("ValueExtractorResultExtractor[methodParameterIndex=1, fetchSize=10]", extractor.toString());
  }

}
