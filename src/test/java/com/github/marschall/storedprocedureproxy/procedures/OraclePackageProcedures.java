package com.github.marschall.storedprocedureproxy.procedures;

import com.github.marschall.storedprocedureproxy.annotations.InOutParameter;
import com.github.marschall.storedprocedureproxy.annotations.Namespace;
import com.github.marschall.storedprocedureproxy.annotations.OutParameter;
import com.github.marschall.storedprocedureproxy.annotations.ProcedureName;
import com.github.marschall.storedprocedureproxy.annotations.ReturnValue;
import com.github.marschall.storedprocedureproxy.annotations.TypeName;

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
  int sum(@TypeName("stored_procedure_proxy.TEST_IDS") int[] ids);

  @OutParameter(name = "sum_result", typeName = "stored_procedure_proxy.TEST_IDS")
  @ProcedureName("array_result")
  int[] arrayResult();

}
