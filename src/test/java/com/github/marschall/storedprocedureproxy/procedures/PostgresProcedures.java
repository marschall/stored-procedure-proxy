package com.github.marschall.storedprocedureproxy.procedures;

import java.sql.SQLException;
import java.util.List;

import com.github.marschall.storedprocedureproxy.ValueExtractor;
import com.github.marschall.storedprocedureproxy.annotations.OutParameter;
import com.github.marschall.storedprocedureproxy.annotations.ProcedureName;
import com.github.marschall.storedprocedureproxy.annotations.ReturnValue;
import com.github.marschall.storedprocedureproxy.annotations.TypeName;

public interface PostgresProcedures {

  @ProcedureName("cs_fmt_browser_version")
  @ReturnValue
  String browserVersion(String name,  String version);

  @ProcedureName("sales_tax")
  @ReturnValue
  float salesTax(float subtotal);

  @ProcedureName("property_tax")
  @OutParameter
  float propertyTax(float subtotal);

  @ProcedureName("raise_exception")
  void raiseCheckedException() throws SQLException;

  @ProcedureName("raise_exception")
  void raiseUncheckedException();

  @ReturnValue
  @ProcedureName("simple_ref_cursor")
  List<String> simpleRefCursor();

  @OutParameter
  @ProcedureName("simple_ref_cursor_out")
  List<String> simpleRefCursorOut();

  @OutParameter
  @ProcedureName("simple_ref_cursor")
  List<String> mappedRefCursor(ValueExtractor<String> extractor);

  @ReturnValue
  @ProcedureName("sample_array_argument")
  String sampleArrayArgumentList(@TypeName("int") List<Integer> ids);

  @ReturnValue
  @ProcedureName("sample_array_argument")
  String sampleArrayArgumentArray(@TypeName("int") Integer[] ids);


}
