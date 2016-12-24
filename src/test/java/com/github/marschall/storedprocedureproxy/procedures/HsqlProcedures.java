package com.github.marschall.storedprocedureproxy.procedures;

import java.sql.Timestamp;
import java.util.List;

import com.github.marschall.storedprocedureproxy.annotations.ProcedureName;

public interface HsqlProcedures {

  @ProcedureName("an_hour_before")
  Timestamp anHourBefore(Timestamp t);

  @ProcedureName("one_two")
  List<Integer> refCursor();

  @ProcedureName("array_cardinality")
  int arrayCardinality(Integer[] integer);

  @ProcedureName("return_array")
  Integer[] returnArray();

}
