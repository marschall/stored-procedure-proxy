package com.github.marschall.springjdbccall.procedures;

import com.github.marschall.springjdbccall.annotations.OutParameter;
import com.github.marschall.springjdbccall.annotations.ProcedureName;
import com.github.marschall.springjdbccall.annotations.ReturnValue;

public interface MysqlProcedures {

  @ProcedureName("hello_function")
  @ReturnValue
  String helloFunction(String s);

  @ProcedureName("hello_procedure")
  @OutParameter
  String helloProcedure(String s);

}
