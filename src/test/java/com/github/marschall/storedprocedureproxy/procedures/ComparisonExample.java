package com.github.marschall.storedprocedureproxy.procedures;

import com.github.marschall.storedprocedureproxy.annotations.OutParameter;

public interface ComparisonExample {

  @OutParameter
  int plus1inout(int arg);

}
