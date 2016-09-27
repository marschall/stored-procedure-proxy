package com.github.marschall.storedprocedureproxy.procedures;

import java.util.List;

import com.github.marschall.storedprocedureproxy.annotations.OutParameter;
import com.github.marschall.storedprocedureproxy.annotations.ProcedureName;
import com.github.marschall.storedprocedureproxy.annotations.ReturnValue;

public interface MysqlProcedures {

  @ProcedureName("hello_function")
  @ReturnValue
  String helloFunction(String s);

  @ProcedureName("hello_procedure")
  @OutParameter
  String helloProcedure(String s);

  @ProcedureName("fake_refcursor")
  List<String> fakeRefcursor();

}
