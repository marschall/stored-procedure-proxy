package com.github.marschall.storedprocedureproxy.examples;

import static org.junit.Assert.assertEquals;

import java.sql.Types;

import javax.sql.DataSource;

import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;
import org.skife.jdbi.v2.OutParameters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;
import org.springframework.transaction.annotation.Transactional;

import com.github.marschall.storedprocedureproxy.configuration.HsqlConfiguration;
import com.github.marschall.storedprocedureproxy.configuration.TestConfiguration;

@Transactional
@ContextConfiguration(classes = {HsqlConfiguration.class, TestConfiguration.class})
public class JdbiTest {

  @ClassRule
  public static final SpringClassRule SPRING_CLASS_RULE = new SpringClassRule();

  @Rule
  public final SpringMethodRule springMethodRule = new SpringMethodRule();

  @Autowired
  private DataSource dataSource;

  private Handle handle;

  @Before
  public void setUp() {
    DBI dbi = new DBI(this.dataSource);
    this.handle = dbi.open();
  }

  @Test
  public void call() {
    assertEquals(2, plus1inout(1));
  }

  @After
  public void tearDown() {
    this.handle.close();
  }

  private int plus1inout(int argument) {
    OutParameters outParameters = handle.createCall("call plus1inout(?, ?);")
            .bind(0, argument)
            .registerOutParameter(1, Types.INTEGER)
            .invoke();
    return outParameters.getInt(2);
  }

}
