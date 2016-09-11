package com.github.marschall.springjdbccall.procedures;

import java.sql.Types;

import com.github.marschall.springjdbccall.annotations.OutParameter;
import com.github.marschall.springjdbccall.annotations.ParameterName;
import com.github.marschall.springjdbccall.annotations.ParameterType;
import com.github.marschall.springjdbccall.annotations.ProcedureName;
import com.github.marschall.springjdbccall.annotations.Schema;

@Schema("SAMPLE_NAME")
public interface SamplePackage {

  @ProcedureName("SAMPLE_FUNCTION")
  void sampleFunction(String argument);

  @ProcedureName("SAMPLE_PROCEDURE")
  @OutParameter
  String sampleProcedure(@ParameterType(Types.NUMERIC) @ParameterName("p_arg_in") int argument);

}
