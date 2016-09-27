package com.github.marschall.storedprocedureproxy.configuration;

import static org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType.DERBY;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;

@Configuration
public class DerbyConfiguration {

  @Bean
  public DataSource dataSource() {
    return new EmbeddedDatabaseBuilder()
            .generateUniqueName(true)
            .setType(DERBY)
            .setScriptEncoding("UTF-8")
            .ignoreFailedDrops(true)
            .addScript("derby_procedures.sql")
            .build();
  }

}
