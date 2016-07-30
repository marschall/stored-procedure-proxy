package com.github.marschall.springjdbccall;

@PackageName("SAMPLE_NAME")
public interface SamplePackage {

  @ProcedureName("SAMPLE_FUNCTION")
  void sampleFunction(String argument);

  @ProcedureName("SAMPLE_PROCEDURE")
  String sampleProcedure(int argument);

}
