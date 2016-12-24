package com.github.marschall.storedprocedureproxy.examples;

import static org.junit.Assert.assertEquals;

import java.sql.Types;
import java.util.Map;

import javax.sql.DataSource;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.object.StoredProcedure;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;
import org.springframework.transaction.annotation.Transactional;

import com.github.marschall.storedprocedureproxy.configuration.HsqlConfiguration;
import com.github.marschall.storedprocedureproxy.configuration.TestConfiguration;

@Transactional
@ContextConfiguration(classes = {HsqlConfiguration.class, TestConfiguration.class})
public class StoredProcedureTest {

  @ClassRule
  public static final SpringClassRule SPRING_CLASS_RULE = new SpringClassRule();

  @Rule
  public final SpringMethodRule springMethodRule = new SpringMethodRule();

  @Autowired
  private DataSource dataSource;

  private Plus1inout storedProcedure;

  @Before
  public void setUp() {
    this.storedProcedure = new Plus1inout(new JdbcTemplate(this.dataSource));
  }

  @Test
  public void call() {
    assertEquals(2, this.storedProcedure.plus1inout(1));
  }

  static final class Plus1inout extends StoredProcedure {

    Plus1inout(JdbcTemplate jdbcTemplate) {
      super(jdbcTemplate, "plus1inout");
      setFunction(false);
      // names have to be supplied even if they are not used
      // constructors are heavily overloaded with String and int
      declareParameter(new SqlParameter("arg", Types.INTEGER));
      declareParameter(new SqlOutParameter("res", Types.INTEGER));
      compile();
    }

    int plus1inout(int arg) {
      Map<String, Object> results = execute(arg);
      return (Integer) results.get("res");
    }

  }

}
