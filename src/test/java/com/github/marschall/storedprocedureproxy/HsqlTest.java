package com.github.marschall.storedprocedureproxy;

import static com.github.marschall.storedprocedureproxy.ProcedureCallerFactory.ParameterRegistration.INDEX_AND_TYPE;
import static com.github.marschall.storedprocedureproxy.ProcedureCallerFactory.ParameterRegistration.INDEX_ONLY;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.sql.DataSource;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;
import org.springframework.transaction.annotation.Transactional;

import com.github.marschall.storedprocedureproxy.ProcedureCallerFactory.ParameterRegistration;
import com.github.marschall.storedprocedureproxy.configuration.HsqlConfiguration;
import com.github.marschall.storedprocedureproxy.configuration.TestConfiguration;
import com.github.marschall.storedprocedureproxy.procedures.HsqlProcedures;

@RunWith(Parameterized.class)
@Transactional
@ContextConfiguration(classes = {HsqlConfiguration.class, TestConfiguration.class})
public class HsqlTest {

  @ClassRule
  public static final SpringClassRule SPRING_CLASS_RULE = new SpringClassRule();

  @Rule
  public final SpringMethodRule springMethodRule = new SpringMethodRule();

  @Autowired
  private DataSource dataSource;

  private HsqlProcedures procedures;

  private ParameterRegistration parameterRegistration;

  public HsqlTest(ParameterRegistration parameterRegistration) {
    this.parameterRegistration = parameterRegistration;
  }

  @Before
  public void setUp() {
    this.procedures = ProcedureCallerFactory.of(HsqlProcedures.class, this.dataSource)
            .withParameterRegistration(this.parameterRegistration)
            .build();
  }

  @Parameters
  public static Collection<Object[]> parameters() {
    return Arrays.asList(
            new Object[] {INDEX_ONLY},
            new Object[] {INDEX_AND_TYPE}
    );
  }

  @Test
  public void function() {
    LocalDateTime after = LocalDateTime.of(2016, 10, 12, 17, 19);
    LocalDateTime before = after.minusHours(1L);
    assertEquals(Timestamp.valueOf(before), this.procedures.anHourBefore(Timestamp.valueOf(after)));
  }

  @Test
  public void refCursor() {
    List<Integer> list = this.procedures.refCursor();
    assertEquals(Arrays.asList(1, 2), list);
  }

  @Test
  public void arrayCardinality() {
    Integer[] array = new Integer[] {1, 2, 3};
    int arrayCardinality = this.procedures.arrayCardinality(array);
    assertEquals(array.length, arrayCardinality);
  }

  @Test
  public void returnArray() {
    Integer[] actual = this.procedures.returnArray();
    Integer[] expected = new Integer[] {0, 5, 10};
    assertArrayEquals(expected, actual);
  }

}
