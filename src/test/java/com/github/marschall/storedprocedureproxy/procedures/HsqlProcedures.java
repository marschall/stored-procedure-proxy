package com.github.marschall.storedprocedureproxy.procedures;

import java.sql.Timestamp;

import com.github.marschall.storedprocedureproxy.annotations.OutParameter;
import com.github.marschall.storedprocedureproxy.annotations.ProcedureName;

public interface HsqlProcedures {

  @OutParameter
  int plus1inout(int arg);

//  @ReturnValue
  @ProcedureName("AN_HOUR_BEFORE")
  Timestamp anHourBefore(Timestamp t);

}
