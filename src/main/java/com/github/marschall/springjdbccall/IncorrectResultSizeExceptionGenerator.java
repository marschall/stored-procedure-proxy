package com.github.marschall.springjdbccall;

interface IncorrectResultSizeExceptionGenerator {

  RuntimeException newIncorrectResultSizeException(int expectedSize, int actualSize);

}
