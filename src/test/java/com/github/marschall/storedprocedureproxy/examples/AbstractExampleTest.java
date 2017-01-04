package com.github.marschall.storedprocedureproxy.examples;

import static org.junit.Assert.assertEquals;

import java.sql.SQLException;

import javax.sql.DataSource;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;
import org.springframework.transaction.annotation.Transactional;

import com.github.marschall.storedprocedureproxy.configuration.HsqlConfiguration;
import com.github.marschall.storedprocedureproxy.configuration.TestConfiguration;

@Transactional
@ContextConfiguration(classes = {HsqlConfiguration.class, TestConfiguration.class})
public abstract class AbstractExampleTest {

  @ClassRule
  public static final SpringClassRule SPRING_CLASS_RULE = new SpringClassRule();

  @Rule
  public final SpringMethodRule springMethodRule = new SpringMethodRule();

  @Autowired
  private DataSource dataSource;

  protected DataSource getDataSource() {
    return this.dataSource;
  }

  @Test
  public void callProcedure() throws SQLException {
    assertEquals(2, this.plus1inout(1));
  }

  protected abstract int plus1inout(int arg) throws SQLException;

}
