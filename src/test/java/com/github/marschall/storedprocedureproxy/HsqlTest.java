package com.github.marschall.storedprocedureproxy;

import static org.junit.Assert.assertEquals;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

import javax.sql.DataSource;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;
import org.springframework.transaction.annotation.Transactional;

import com.github.marschall.storedprocedureproxy.configuration.HsqlConfiguration;
import com.github.marschall.storedprocedureproxy.configuration.TestConfiguration;
import com.github.marschall.storedprocedureproxy.procedures.HsqlProcedures;

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

  @Before
  public void setUp() {
    this.procedures = ProcedureCallerFactory.build(HsqlProcedures.class, this.dataSource);
  }

  @Test
  public void procedure() {
    assertEquals(2, this.procedures.plus1inout(1));
  }

  @Test
  @Ignore("buggy")
  public void function() throws SQLException {
    try (Connection connection = this.dataSource.getConnection()) {
      DatabaseMetaData metaData = connection.getMetaData();
      try (ResultSet functions = metaData.getFunctions(null, null, null)) {
        while (functions.next()) {
          System.out.println(functions.getString("FUNCTION_NAME"));
        }
      }
//      try (CallableStatement call = connection.prepareCall("{ call an_hour_before(?)}")) {
//
//      }
      try (CallableStatement call = connection.prepareCall("{ ? = call an_hour_before(?)}")) {

      }
    }
    LocalDateTime after = LocalDateTime.of(2016, 10, 12, 17, 19);
    LocalDateTime before = after.minusHours(1L);
    assertEquals(Timestamp.valueOf(before), this.procedures.anHourBefore(Timestamp.valueOf(before)));
  }

}
