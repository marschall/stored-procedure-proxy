package com.github.marschall.storedprocedureproxy;

final class ByteUtils {

  private ByteUtils() {
    throw new AssertionError("not instantiable");
  }

  static int toInt(byte b) {
    return Byte.toUnsignedInt(b);
  }

  static byte toByte(int i) {
    if (i < 0 || i > 255) {
      throw new IllegalArgumentException();
    }
    return (byte) i;
  }

  static void toStringOn(byte[] array, StringBuilder builder) {
    for (int i = 0; i < array.length; i++) {
      if (i > 0) {
        builder.append(", ");
      }
      int element = toInt(array[i]);
      builder.append(element);
    }
  }

}
