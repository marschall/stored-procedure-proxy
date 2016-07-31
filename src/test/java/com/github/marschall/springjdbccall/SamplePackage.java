package com.github.marschall.springjdbccall;

import java.sql.Types;

import com.github.marschall.springjdbccall.annotations.OutParameter;
import com.github.marschall.springjdbccall.annotations.PackageName;
import com.github.marschall.springjdbccall.annotations.ParameterName;
import com.github.marschall.springjdbccall.annotations.ParameterType;
import com.github.marschall.springjdbccall.annotations.ProcedureName;

@PackageName("SAMPLE_NAME")
public interface SamplePackage {

  @ProcedureName("SAMPLE_FUNCTION")
  void sampleFunction(String argument);

  @ProcedureName("SAMPLE_PROCEDURE")
  @OutParameter
  String sampleProcedure(@ParameterType(Types.NUMERIC) @ParameterName("p_arg_in") int argument);

}
