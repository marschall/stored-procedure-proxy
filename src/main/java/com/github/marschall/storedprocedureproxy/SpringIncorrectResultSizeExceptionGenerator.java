package com.github.marschall.storedprocedureproxy;

import org.springframework.dao.IncorrectResultSizeDataAccessException;

final class SpringIncorrectResultSizeExceptionGenerator implements IncorrectResultSizeExceptionGenerator {

  @Override
  public RuntimeException newIncorrectResultSizeException(int expectedSize, int actualSize) {
    return new IncorrectResultSizeDataAccessException(expectedSize, actualSize);
  }

}
