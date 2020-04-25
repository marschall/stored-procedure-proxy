package com.github.marschall.storedprocedureproxy.procedures;

import java.util.List;

import com.github.marschall.storedprocedureproxy.annotations.InOutParameter;
import com.github.marschall.storedprocedureproxy.annotations.Namespace;
import com.github.marschall.storedprocedureproxy.annotations.OutParameter;
import com.github.marschall.storedprocedureproxy.annotations.ProcedureName;
import com.github.marschall.storedprocedureproxy.annotations.ReturnValue;
import com.github.marschall.storedprocedureproxy.annotations.TypeName;

import oracle.jdbc.OracleTypes;

@Namespace("stored_procedure_proxy")
public interface OraclePackageProcedures {

  @ReturnValue
  @ProcedureName("negate_function")
  boolean negateFunction(boolean b);

  @InOutParameter
  @ProcedureName("negate_procedure")
  boolean negateProcedure(boolean b);

  @OutParameter(name = "sum_result")
  @ProcedureName("array_sum")
  int sum(@TypeName("STORED_PROCEDURE_PROXY_ARRAY") int[] ids);

  @OutParameter(name = "ARRAY_RESULT", typeName = "STORED_PROCEDURE_PROXY_ARRAY")
  @ProcedureName("return_array")
  int[] arrayResult();

  @OutParameter(name = "ids", type = OracleTypes.CURSOR)
  @ProcedureName("return_refcursor")
  List<Integer> returnRefcursor();

}
