package com.github.marschall.springjdbccall;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.SQLXML;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;

import com.github.marschall.springjdbccall.spi.TypeMapper;

enum DefaultTypeMapper implements TypeMapper {
  INSTANCE;

  @Override
  public int mapToSqlType(Class<?> javaType) {
    // no need to handle primitives since everything is boxed anyway
    if (javaType == String.class) {
      return Types.VARCHAR;

    // primitive integers
    } else if (javaType == Integer.class) {
      return Types.INTEGER;
    } else if (javaType == Long.class) {
      return Types.BIGINT;
    } else if (javaType == Short.class) {
      return Types.SMALLINT;
    } else if (javaType == Byte.class) {

    // arbitrary precision numbers
      return Types.TINYINT;
    } else if (javaType == BigDecimal.class) {
      // should be an alias for DECIMAL but Oracle treats DECIMAL as double
      return Types.NUMERIC;
    } else if (javaType == BigInteger.class) {
      return Types.NUMERIC;

    // floating points
    } else if (javaType == Float.class) {
      return Types.REAL;
    } else if (javaType == Double.class) {
      return Types.DOUBLE;

    // LOBs
    } else if (javaType == Blob.class) {
      return Types.BLOB;
    } else if (javaType == Clob.class) {
      return Types.CLOB;

    // java 8 date time
    } else if (javaType == LocalDate.class) {
      return Types.DATE;
    } else if (javaType == LocalTime.class) {
      return Types.TIME;
    } else if (javaType == LocalDateTime.class) {
      return Types.TIMESTAMP;
    } else if (javaType == OffsetTime.class) {
      return Types.TIME_WITH_TIMEZONE;
    } else if (javaType == OffsetDateTime.class) {
      return Types.TIMESTAMP_WITH_TIMEZONE;

    // old date time
    } else if (javaType == java.sql.Date.class) {
      return Types.DATE;
    } else if (javaType == java.sql.Time.class) {
      return Types.TIME;
    } else if (javaType == java.sql.Timestamp.class) {
      return Types.TIMESTAMP;

    } else if (javaType == SQLXML.class) {
      return Types.SQLXML;
    // boolean
    } else if (javaType == Boolean.class) {
      return Types.BOOLEAN;
    }

    throw new IllegalArgumentException("unknown type: " + javaType);
  }


}
