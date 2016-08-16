package com.github.marschall.springjdbccall;

import java.sql.SQLException;

import com.github.marschall.springjdbccall.annotations.OutParameter;
import com.github.marschall.springjdbccall.annotations.ProcedureName;
import com.github.marschall.springjdbccall.annotations.ReturnValue;

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

}
