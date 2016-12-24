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
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.object.GenericStoredProcedure;
import org.springframework.jdbc.object.StoredProcedure;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;
import org.springframework.transaction.annotation.Transactional;

import com.github.marschall.storedprocedureproxy.configuration.HsqlConfiguration;
import com.github.marschall.storedprocedureproxy.configuration.TestConfiguration;

@Transactional
@ContextConfiguration(classes = {HsqlConfiguration.class, TestConfiguration.class})
public class GenericStoredProcedureTest {

  @ClassRule
  public static final SpringClassRule SPRING_CLASS_RULE = new SpringClassRule();

  @Rule
  public final SpringMethodRule springMethodRule = new SpringMethodRule();

  @Autowired
  private DataSource dataSource;

  private StoredProcedure storedProcedure;

  @Before
  public void setUp() {
    storedProcedure = new GenericStoredProcedure();
    storedProcedure.setDataSource(dataSource);
    storedProcedure.setSql("plus1inout");
    storedProcedure.setFunction(false);

    storedProcedure.declareParameter(new SqlParameter("arg", Types.INTEGER));
    storedProcedure.declareParameter(new SqlOutParameter("res", Types.INTEGER));
    storedProcedure.compile();
  }

  @Test
  public void call() {
    assertEquals(2, this.plus1inout(1));
  }


  private int plus1inout(int arg) {
    Map<String, Object> results = this.storedProcedure.execute(arg);
    return (Integer) results.get("res");
  }

}
