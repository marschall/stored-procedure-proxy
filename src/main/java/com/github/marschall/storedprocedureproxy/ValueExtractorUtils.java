package com.github.marschall.storedprocedureproxy;

final class ValueExtractorUtils {

  private ValueExtractorUtils() {
    throw new AssertionError("not instantiable");
  }

  static boolean isAnyValueExtractor(Class<?> clazz) {
    return isValueExtractor(clazz)
//            || isFunction(clazz)
            || isNumberedValueExtractor(clazz);
  }

  static boolean isValueExtractor(Class<?> clazz) {
    return clazz.isAssignableFrom(ValueExtractor.class);
  }

  static boolean isNumberedValueExtractor(Class<?> clazz) {
    return clazz.isAssignableFrom(NumberedValueExtractor.class);
  }

//  static boolean isFunction(Class<?> clazz) {
//    return clazz.isAssignableFrom(Function.class);
//  }

}
