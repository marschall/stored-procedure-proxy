package com.github.marschall.storedprocedureproxy.procedures;

import java.sql.Types;

import com.github.marschall.storedprocedureproxy.annotations.OutParameter;
import com.github.marschall.storedprocedureproxy.annotations.ParameterName;
import com.github.marschall.storedprocedureproxy.annotations.ParameterType;
import com.github.marschall.storedprocedureproxy.annotations.ProcedureName;
import com.github.marschall.storedprocedureproxy.annotations.Schema;

@Schema("SAMPLE_NAME")
public interface SamplePackage {

  @ProcedureName("SAMPLE_FUNCTION")
  void sampleFunction(String argument);

  @ProcedureName("SAMPLE_PROCEDURE")
  @OutParameter
  String sampleProcedure(@ParameterType(Types.NUMERIC) @ParameterName("p_arg_in") int argument);

}
