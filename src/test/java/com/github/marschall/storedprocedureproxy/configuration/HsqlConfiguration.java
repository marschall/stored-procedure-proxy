package com.github.marschall.storedprocedureproxy.configuration;

import static org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType.HSQL;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;

@Configuration
public class HsqlConfiguration {

  @Bean
  public DataSource dataSource() {
    return new EmbeddedDatabaseBuilder()
            .generateUniqueName(true)
            .setType(HSQL)
            .setScriptEncoding("UTF-8")
            .ignoreFailedDrops(true)
            .setSeparator("/;")
            .addScript("sql/hsql_procedures.sql")
            .build();
  }

}
