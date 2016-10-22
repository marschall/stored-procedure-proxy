package com.github.marschall.storedprocedureproxy.spi;

import java.sql.Types;

import com.github.marschall.storedprocedureproxy.annotations.OutParameter;
import com.github.marschall.storedprocedureproxy.annotations.ParameterType;
import com.github.marschall.storedprocedureproxy.annotations.ReturnValue;

/**
 * Maps a Java type to a SQL type. This is especially useful if you
 * want to use vendor types.
 *
 * <p>The mapping defined by an instance of this class will be applied
 * globally to all in and out parameter of all methods in an interface.
 * If you want to customize only a single parameter use
 * {@link ParameterType}, {@link OutParameter#type()} or
 * {@link ReturnValue#type()} instead.</p>
 *
 * <p>If no custom implementation is specified the following default
 * are used:</p>
 * <table>
 * <caption>default type mappings</caption>
 * <tr><th>Java Type</th><th>SQL type</th></tr>
 * <tr><td>String</td><td>{@link Types#VARCHAR}</td></tr>
 * <tr><td colspan="2">char is not mapped</td></tr>
 * <tr><td colspan="2">limited precision integers</td></tr>
 * <tr><td>Integer</td><td>{@link Types#INTEGER}</td></tr>
 * <tr><td>int</td><td>{@link Types#INTEGER}</td></tr>
 * <tr><td>Long</td><td>{@link Types#BIGINT}</td></tr>
 * <tr><td>long</td><td>{@link Types#BIGINT}</td></tr>
 * <tr><td>Short</td><td>{@link Types#SMALLINT}</td></tr>
 * <tr><td>short</td><td>{@link Types#SMALLINT}</td></tr>
 * <tr><td>Byte</td><td>{@link Types#TINYINT}</td></tr>
 * <tr><td>byte</td><td>{@link Types#TINYINT}</td></tr>
 * <tr><td colspan="2">arbitrary precision numbers</td></tr>
 * <tr><td colspan="2">should be an alias for DECIMAL but Oracle treats DECIMAL as double</td></tr>
 * <tr><td>BigDecimal</td><td>{@link Types#NUMERIC}</td></tr>
 * <tr><td>BigInteger</td><td>{@link Types#NUMERIC}</td></tr>

 * <tr><td colspan="2">floating points</td></tr>
 * <tr><td>Float</td><td>{@link Types#REAL}</td></tr>
 * <tr><td>Double</td><td>{@link Types#DOUBLE}</td></tr>
 * <tr><td>float</td><td>{@link Types#REAL}</td></tr>
 * <tr><td>double</td><td>{@link Types#DOUBLE}</td></tr>

 * <tr><td colspan="2">LOBs</td></tr>
 * <tr><td>Blob</td><td>{@link Types#BLOB}</td></tr>
 * <tr><td>Clob</td><td>{@link Types#CLOB}</td></tr>
 * <tr><td>NClob</td><td>{@link Types#NCLOB}</td></tr>

 * <tr><td colspan="2">java 8 date time</td></tr>
 * <tr><td>LocalDate</td><td>{@link Types#DATE}</td></tr>
 * <tr><td>LocalTime</td><td>{@link Types#TIME}</td></tr>
 * <tr><td>LocalDateTime</td><td>{@link Types#TIMESTAMP}</td></tr>
 * <tr><td>OffsetTime</td><td>{@link Types#TIME_WITH_TIMEZONE}</td></tr>
 * <tr><td>OffsetDateTime</td><td>{@link Types#TIMESTAMP_WITH_TIMEZONE}</td></tr>

 * <tr><td colspan="2">old date time</td></tr>
 * <tr><td>java.sql.Date</td><td>{@link Types#DATE}</td></tr>
 * <tr><td>java.sql.Time</td><td>{@link Types#TIME}</td></tr>
 * <tr><td>java.sql.Timestamp</td><td>{@link Types#TIMESTAMP}</td></tr>

 * <tr><td colspan="2">XML</td></tr>
 * <tr><td>SQLXML</td><td>{@link Types#SQLXML}</td></tr>
 * <tr><td colspan="2">boolean</td></tr>
 * <tr><td>Boolean</td><td>{@link Types#BOOLEAN}</td></tr>
 * <tr><td>boolean</td><td>{@link Types#BOOLEAN}</td></tr>
 * </table>
 */
@FunctionalInterface
public interface TypeMapper {

  /**
   * Maps a Java type to a SQL type.
   *
   * @see java.sql.Types
   * @param javaType
   *          the java type, may be a primitive type like {@code int.class},
   *          never {@code null}, never {@code void.class}
   * @return the SQL type, may be a vendor type
   */
  int mapToSqlType(Class<?> javaType);

}
