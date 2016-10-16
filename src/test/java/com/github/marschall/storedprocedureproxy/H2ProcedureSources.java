package com.github.marschall.storedprocedureproxy;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.logging.Logger;

import org.h2.tools.SimpleResultSet;

public class H2ProcedureSources {

  private static final Logger LOG = Logger.getLogger("H2ProcedureSources");

  public static String stringProcedure(String input) {
    return "pre" + input + "post";
  }

  public static void voidProcedure(String input) {
    LOG.fine(input);
  }

  public static String noArgProcedure() {
    return "output";
  }

  public static ResultSet simpleResultSet() throws SQLException {
    SimpleResultSet resultSet = new SimpleResultSet();
    resultSet.addColumn("ID", Types.INTEGER, 10, 0);
    resultSet.addColumn("NAME", Types.VARCHAR, 255, 0);
    resultSet.addRow(0L, "Hello");
    resultSet.addRow(1L, "World");
    return resultSet;
  }

}
