package com.github.marschall.springjdbccall;

final class DefaultIncorrectResultSizeExceptionGenerator implements IncorrectResultSizeExceptionGenerator {

  @Override
  public RuntimeException newIncorrectResultSizeException(int expectedSize, int actualSize) {
    return new IncorrectResultSizeException(expectedSize, actualSize);
  }

}
