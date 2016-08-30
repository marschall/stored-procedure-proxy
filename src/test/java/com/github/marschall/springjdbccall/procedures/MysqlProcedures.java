package com.github.marschall.springjdbccall.procedures;

import java.sql.Types;
import java.util.List;

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

  @ProcedureName("fake_refcursor")
  @ReturnValue(type = Types.OTHER)
  List<String> fakeRefcursor();

}
