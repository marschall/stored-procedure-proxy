package com.github.marschall.springjdbccall;

import java.sql.Types;

import com.github.marschall.springjdbccall.annotations.PackageName;
import com.github.marschall.springjdbccall.annotations.ParameterType;
import com.github.marschall.springjdbccall.annotations.ProcedureName;

@PackageName("SAMPLE_NAME")
public interface SamplePackage {

  @ProcedureName("SAMPLE_FUNCTION")
  void sampleFunction(String argument);

  @ProcedureName("SAMPLE_PROCEDURE")
  String sampleProcedure(@ParameterType(Types.NUMERIC) int argument);

}
