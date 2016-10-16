package com.github.marschall.storedprocedureproxy.procedures;

import com.github.marschall.storedprocedureproxy.annotations.ReturnValue;

public interface H2Procedures {

  @ReturnValue
  String stringProcedure(String input);

  void voidProcedure(String input);

  @ReturnValue
  String noArgProcedure();

}
