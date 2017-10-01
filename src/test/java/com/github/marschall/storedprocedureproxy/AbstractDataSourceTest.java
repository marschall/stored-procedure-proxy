package com.github.marschall.storedprocedureproxy;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.annotation.Transactional;

import com.github.marschall.storedprocedureproxy.configuration.TestConfiguration;

@Transactional
@SpringJUnitConfig(TestConfiguration.class)
public abstract class AbstractDataSourceTest {

  @Autowired
  private DataSource dataSource;

  protected DataSource getDataSource() {
    return dataSource;
  }

}
