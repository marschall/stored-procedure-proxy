package com.github.marschall.storedprocedureproxy;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.github.marschall.storedprocedureproxy.ProcedureCallerFactory.ProcedureCaller;

public class ListResultExtractorTest {

  @Test
  public void testToString()  {
    ResultExtractor extractor = new ListResultExtractor(Integer.class, ProcedureCaller.DEFAULT_FETCH_SIZE);
    assertEquals("ListResultExtractor[type=Integer, fetchSize=default]", extractor.toString());

    extractor = new ListResultExtractor(Integer.class, 10);
    assertEquals("ListResultExtractor[type=Integer, fetchSize=10]", extractor.toString());
  }

}
