package com.github.marschall.storedprocedureproxy;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.NClob;
import java.sql.SQLXML;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.github.marschall.storedprocedureproxy.spi.TypeMapper;

final class DefaultTypeMapper implements TypeMapper {

  static final TypeMapper INSTANCE = new DefaultTypeMapper();

  private final Map<Class<?>, Integer> typeMap;

  DefaultTypeMapper() {
    this.typeMap = new HashMap<>();
    this.typeMap.put(String.class, Types.VARCHAR);

    // char is not mapped

    // limited precision integers
    this.typeMap.put(Integer.class, Types.INTEGER);
    this.typeMap.put(int.class, Types.INTEGER);
    this.typeMap.put(Long.class, Types.BIGINT);
    this.typeMap.put(long.class, Types.BIGINT);
    this.typeMap.put(Short.class, Types.SMALLINT);
    this.typeMap.put(short.class, Types.SMALLINT);
    this.typeMap.put(Byte.class, Types.TINYINT);
    this.typeMap.put(byte.class, Types.TINYINT);
    // arbitrary precision numbers
    // should be an alias for DECIMAL but Oracle treats DECIMAL as double
    this.typeMap.put(BigDecimal.class, Types.NUMERIC);
    this.typeMap.put(BigInteger.class, Types.NUMERIC);

    // floating points
    this.typeMap.put(Float.class, Types.REAL);
    this.typeMap.put(float.class, Types.REAL);
    this.typeMap.put(Double.class, Types.DOUBLE);
    this.typeMap.put(double.class, Types.DOUBLE);

    // LOBs
    this.typeMap.put(Blob.class, Types.BLOB);
    this.typeMap.put(Clob.class, Types.CLOB);
    this.typeMap.put(NClob.class, Types.NCLOB);

    // java 8 date time
    this.typeMap.put(LocalDate.class, Types.DATE);
    this.typeMap.put(LocalTime.class, Types.TIME);
    this.typeMap.put(LocalDateTime.class, Types.TIMESTAMP);
    this.typeMap.put(OffsetTime.class, Types.TIME_WITH_TIMEZONE);
    this.typeMap.put(OffsetDateTime.class, Types.TIMESTAMP_WITH_TIMEZONE);

    // old date time
    this.typeMap.put(java.sql.Date.class, Types.DATE);
    this.typeMap.put(java.sql.Time.class, Types.TIME);
    this.typeMap.put(java.sql.Timestamp.class, Types.TIMESTAMP);

    this.typeMap.put(SQLXML.class, Types.SQLXML);
    // boolean
    this.typeMap.put(Boolean.class, Types.BOOLEAN);
    this.typeMap.put(boolean.class, Types.BOOLEAN);

    // array
    this.typeMap.put(Collection.class, Types.ARRAY);
    this.typeMap.put(Set.class, Types.ARRAY);
    this.typeMap.put(List.class, Types.ARRAY);
  }

  @Override
  public int mapToSqlType(Class<?> javaType) {
    Integer sqlType = this.typeMap.get(javaType);
    if (sqlType == null) {
      throw new IllegalArgumentException("unknown type: " + javaType);
    }
    return sqlType;
  }


}
