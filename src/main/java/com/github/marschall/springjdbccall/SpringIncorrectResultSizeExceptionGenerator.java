package com.github.marschall.springjdbccall;

import org.springframework.dao.IncorrectResultSizeDataAccessException;

final class SpringIncorrectResultSizeExceptionGenerator implements IncorrectResultSizeExceptionGenerator {

  @Override
  public RuntimeException newIncorrectResultSizeException(int expectedSize, int actualSize) {
    return new IncorrectResultSizeDataAccessException(expectedSize, actualSize);
  }

}
