package com.github.marschall.springjdbccall;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import javax.sql.DataSource;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;
import org.springframework.transaction.annotation.Transactional;

import com.github.marschall.springjdbccall.configuration.MysqlConfiguration;
import com.github.marschall.springjdbccall.configuration.TestConfiguration;
import com.github.marschall.springjdbccall.procedures.MysqlProcedures;

@Transactional
@Sql("classpath:mysql_procedures.sql")
@ContextConfiguration(classes = {MysqlConfiguration.class, TestConfiguration.class})
public class MysqlTest {

  @ClassRule
  public static final SpringClassRule SPRING_CLASS_RULE = new SpringClassRule();

  @Rule
  public final SpringMethodRule springMethodRule = new SpringMethodRule();

  @Autowired
  private DataSource dataSource;

  private MysqlProcedures procedures;

  @Before
  public void setUp() {
    this.procedures = ProcedureCallerFactory.of(MysqlProcedures.class, this.dataSource)
            .build();
  }

  @Test
  public void helloFunction() {
    assertEquals("Hello, Monty!", this.procedures.helloFunction("Monty"));
  }

  @Test
  public void helloProcedure() {
    assertEquals("Hello, Monty!", this.procedures.helloProcedure("Monty"));
  }

  @Test
  public void manualCall() throws SQLException {
    try (Connection con = this.dataSource.getConnection()) {
//      String query = "{? = CALL fake_refcursor()}";
      String query = "{CALL fake_refcursor()}";
      try (CallableStatement cs = con.prepareCall(query)) {
//        cs.registerOutParameter(1, Types.OTHER);
        boolean hasResultSet = cs.execute();
        assertTrue(hasResultSet);
        // try (ResultSet rs = cs.executeQuery()) {
        try (ResultSet rs = cs.getResultSet()) {
          int count = 0;
          while (rs.next()) {
            assertNotNull(rs.getObject(1));
            count += 1;
          }
          assertEquals(2, count);
        }
      }
    }
  }

  @Test
  public void simpleRefCursor() {
    // http://stackoverflow.com/questions/273929/what-is-the-equivalent-of-oracle-s-ref-cursor-in-mysql-when-using-jdbc
    List<String> refCursor = this.procedures.fakeRefcursor();
    assertEquals(Arrays.asList("hello", "mysql"), refCursor);
  }

}
