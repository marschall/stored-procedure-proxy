package com.github.marschall.springjdbccall;

@FunctionalInterface
interface IncorrectResultSizeExceptionGenerator {

  RuntimeException newIncorrectResultSizeException(int expectedSize, int actualSize);

}
