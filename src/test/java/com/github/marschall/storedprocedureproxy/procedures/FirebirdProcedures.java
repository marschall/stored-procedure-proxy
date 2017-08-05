package com.github.marschall.storedprocedureproxy.procedures;

import java.util.List;

import com.github.marschall.storedprocedureproxy.annotations.OutParameter;
import com.github.marschall.storedprocedureproxy.annotations.ProcedureName;
import com.github.marschall.storedprocedureproxy.annotations.ReturnValue;

public interface FirebirdProcedures {

  int increment(int y);

  @OutParameter(name = "x")
  @ProcedureName("increment")
  int incrementOutParameter(int y);

  @ReturnValue
  @ProcedureName("increment")
  int incrementReturnValue(int y);

  List<Integer> factorial(int maxValue);

}
