package com.github.marschall.springjdbccall;

import java.io.IOException;
import java.io.Reader;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.springframework.core.io.support.EncodedResource;
import org.springframework.jdbc.datasource.init.CannotReadScriptException;
import org.springframework.jdbc.datasource.init.DatabasePopulator;
import org.springframework.jdbc.datasource.init.ScriptException;

public class PostgresDatabasePopulator implements DatabasePopulator {

  private final String separator;
  private final EncodedResource[] resources;

  public PostgresDatabasePopulator(String separator, EncodedResource... resources) {
    this.separator = separator;
    this.resources = resources;
  }

  @Override
  public void populate(Connection connection) throws SQLException, ScriptException {
    try (Statement statement = connection.createStatement()) {
      for (EncodedResource resource : this.resources) {
        loadResource(statement, resource);
      }
    }

  }

  private void loadResource(Statement statement, EncodedResource resource) throws SQLException {
    String content = readResource(resource);
    for (String sql : split(content, this.separator)) {
      if (!sql.trim().isEmpty()) {
        statement.execute(sql);
      }
    }
  }

  static List<String> split(String s, String separator) {
    List<String> result = new ArrayList<>();
    int index = 0;
    while (true) {
      int nextIndex = s.indexOf(separator, index);
      if (nextIndex == -1) {
        if (index < s.length() -1) {
          result.add(s.substring(index, s.length()));
        }
        break;
      }
      result.add(s.substring(index, nextIndex + separator.length()));
      index = nextIndex + separator.length();
    }
    return result;
  }

  private String readResource(EncodedResource resource) throws ScriptException {
    StringBuilder builder = new StringBuilder();
    char[] buffer = new char[1024];
    try (Reader reader = resource.getReader()) {
      for (int read = reader.read(buffer); read != -1; read = reader.read(buffer)) {
        builder.append(buffer, 0, read);
      }
    } catch (IOException e) {
      throw new CannotReadScriptException(resource, e);
    }
    return builder.toString();
  }

}
