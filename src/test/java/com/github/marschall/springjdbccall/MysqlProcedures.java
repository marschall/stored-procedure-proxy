package com.github.marschall.springjdbccall;

import com.github.marschall.springjdbccall.annotations.ProcedureName;

public interface MysqlProcedures {

  @ProcedureName("hello_function")
  String helloFunction(String s);

  @ProcedureName("hello_procedure")
  String helloProcedure(String s);

}
