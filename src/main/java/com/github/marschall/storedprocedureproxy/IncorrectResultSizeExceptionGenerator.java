package com.github.marschall.storedprocedureproxy;

@FunctionalInterface
interface IncorrectResultSizeExceptionGenerator {

  RuntimeException newIncorrectResultSizeException(int expectedSize, int actualSize);

}
