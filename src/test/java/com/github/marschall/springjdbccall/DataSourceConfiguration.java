package com.github.marschall.springjdbccall;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.transaction.PlatformTransactionManager;

import static org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType.H2;

@Configuration
public class DataSourceConfiguration {

  @Bean
  public DataSource dataSource() {
    return new EmbeddedDatabaseBuilder()
            .generateUniqueName(true)
            .setType(H2)
            .setScriptEncoding("UTF-8")
            .ignoreFailedDrops(true)
            .addScript("h2_functions.sql")
            .build();
  }

  @Bean
  public PlatformTransactionManager txManager() {
    return new DataSourceTransactionManager(this.dataSource());
  }

  @Bean
  public JdbcOperations jdbcOperations() {
    return new JdbcTemplate(this.dataSource());
  }
}
