package com.github.marschall.storedprocedureproxy.procedures;

import java.util.List;

import com.github.marschall.storedprocedureproxy.annotations.OutParameter;
import com.github.marschall.storedprocedureproxy.annotations.ParameterName;
import com.github.marschall.storedprocedureproxy.annotations.ProcedureName;
import com.github.marschall.storedprocedureproxy.annotations.ReturnValue;

public interface MysqlProcedures {

  @ProcedureName("hello_function")
  @ReturnValue
  String helloFunction(@ParameterName("s") String s);

  @ProcedureName("hello_procedure")
  @OutParameter(name = "result")
  String helloProcedure(@ParameterName("s") String s);

  @ProcedureName("fake_refcursor")
  List<String> fakeRefcursor();

}
