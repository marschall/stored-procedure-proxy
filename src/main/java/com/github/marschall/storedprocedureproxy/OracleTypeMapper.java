package com.github.marschall.storedprocedureproxy;

import com.github.marschall.storedprocedureproxy.spi.TypeMapper;

/**
 * Like {@link DefaultTypeMapper} but maps {@code boolean} to
 * <a href="https://docs.oracle.com/database/122/JAJDB/constant-values.html#oracle_jdbc_OracleTypes_PLSQL_BOOLEAN">252</a>.
 *
 * @see <a href="https://docs.oracle.com/database/122/JAJDB/oracle/jdbc/OracleTypes.html#PLSQL_BOOLEAN">OracleTypes.PLSQL_BOOLEAN</a>
 */
final class OracleTypeMapper implements TypeMapper {

  static final TypeMapper INSTANCE = new OracleTypeMapper();

  private static final int PLSQL_BOOLEAN = 252;

  private OracleTypeMapper() {
    // private constructor, avoid instantiation
    super();
  }

  @Override
  public int mapToSqlType(Class<?> javaType) {
    if (javaType == Boolean.class || javaType == boolean.class) {
      return PLSQL_BOOLEAN;
    }
    return DefaultTypeMapper.INSTANCE.mapToSqlType(javaType);
  }

}
