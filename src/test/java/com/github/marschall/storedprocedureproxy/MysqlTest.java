package com.github.marschall.storedprocedureproxy;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;

import com.github.marschall.storedprocedureproxy.ProcedureCallerFactory.ParameterRegistration;
import com.github.marschall.storedprocedureproxy.configuration.MysqlConfiguration;
import com.github.marschall.storedprocedureproxy.procedures.MysqlProcedures;

@Sql("classpath:mysql_procedures.sql")
@ContextConfiguration(classes = MysqlConfiguration.class)
public class MysqlTest extends AbstractDataSourceTest {

  private MysqlProcedures procedures(ParameterRegistration parameterRegistration) {
    return ProcedureCallerFactory.of(MysqlProcedures.class, this.getDataSource())
            .withParameterRegistration(parameterRegistration)
            .build();
  }

  public static Stream<ParameterRegistration> parameters() {
    return Stream.of(
            ParameterRegistration.INDEX_ONLY,
            ParameterRegistration.INDEX_AND_TYPE,
            ParameterRegistration.NAME_ONLY,
            ParameterRegistration.NAME_AND_TYPE
    );
  }

  public static Stream<ParameterRegistration> indexParameters() {
    return Stream.of(
            ParameterRegistration.INDEX_ONLY,
            ParameterRegistration.INDEX_AND_TYPE
            );
  }

  @ParameterizedTest
  @MethodSource("indexParameters")
  public void helloFunction(ParameterRegistration parameterRegistration) {
    // names for out parameters of functions don't work

    assertEquals("Hello, Monty!", this.procedures(parameterRegistration).helloFunction("Monty"));
  }

  @ParameterizedTest
  @MethodSource("parameters")
  public void helloProcedure(ParameterRegistration parameterRegistration) {
    assertEquals("Hello, Monty!", this.procedures(parameterRegistration).helloProcedure("Monty"));
  }

  @ParameterizedTest
  @MethodSource("parameters")
  public void simpleRefCursor(ParameterRegistration parameterRegistration) {
    // https://stackoverflow.com/questions/273929/what-is-the-equivalent-of-oracle-s-ref-cursor-in-mysql-when-using-jdbc
    List<String> refCursor = this.procedures(parameterRegistration).fakeRefcursor();
    assertEquals(Arrays.asList("hello", "mysql"), refCursor);
  }

}
