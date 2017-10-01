package com.github.marschall.storedprocedureproxy;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.test.context.ContextConfiguration;

import com.github.marschall.storedprocedureproxy.ProcedureCallerFactory.ParameterRegistration;
import com.github.marschall.storedprocedureproxy.configuration.HsqlConfiguration;
import com.github.marschall.storedprocedureproxy.procedures.HsqlProcedures;

@ContextConfiguration(classes = HsqlConfiguration.class)
public class HsqlTest extends AbstractDataSourceTest {

  private HsqlProcedures procedures(ParameterRegistration parameterRegistration) {
    return ProcedureCallerFactory.of(HsqlProcedures.class, this.getDataSource())
            .withParameterRegistration(parameterRegistration)
            .build();
  }

  public static Stream<ParameterRegistration> parameters() {
    return Stream.of(ParameterRegistration.INDEX_ONLY, ParameterRegistration.INDEX_AND_TYPE);
  }

  @ParameterizedTest
  @MethodSource("parameters")
  public void function(ParameterRegistration parameterRegistration) {
    LocalDateTime after = LocalDateTime.of(2016, 10, 12, 17, 19);
    LocalDateTime before = after.minusHours(1L);
    assertEquals(Timestamp.valueOf(before), this.procedures(parameterRegistration).anHourBefore(Timestamp.valueOf(after)));
  }

  @ParameterizedTest
  @MethodSource("parameters")
  public void refCursor(ParameterRegistration parameterRegistration) {
    List<Integer> list = this.procedures(parameterRegistration).refCursor();
    assertEquals(Arrays.asList(1, 2), list);
  }

  @ParameterizedTest
  @MethodSource("parameters")
  public void arrayCardinality(ParameterRegistration parameterRegistration) {
    Integer[] array = new Integer[] {1, 2, 3};
    int arrayCardinality = this.procedures(parameterRegistration).arrayCardinality(array);
    assertEquals(array.length, arrayCardinality);
  }

  @ParameterizedTest
  @MethodSource("parameters")
  public void returnArray(ParameterRegistration parameterRegistration) {
    Integer[] actual = this.procedures(parameterRegistration).returnArray();
    Integer[] expected = new Integer[] {0, 5, 10};
    assertArrayEquals(expected, actual);
  }

}
