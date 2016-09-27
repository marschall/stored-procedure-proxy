package com.github.marschall.storedprocedureproxy;

import java.util.logging.Logger;

public class H2ProcedureSources {

  private static final Logger LOG = Logger.getLogger("H2ProcedureSources");

  public static String stringProcedure(String input) {
    return "pre" + input + "post";
  }

  public static void voidProcedure(String input) {
    LOG.fine(input);
  }

}
