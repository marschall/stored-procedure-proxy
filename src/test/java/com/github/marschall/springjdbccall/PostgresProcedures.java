package com.github.marschall.springjdbccall;

import com.github.marschall.springjdbccall.annotations.ProcedureName;
import com.github.marschall.springjdbccall.annotations.ReturnValue;

public interface PostgresProcedures {

  @ProcedureName("cs_fmt_browser_version")
  @ReturnValue
  String browserVersion(String name,  String version);

  @ProcedureName("sales_tax")
  double salesTax(double subtotal);

  @ProcedureName("property_tax")
  double propertyTax(double subtotal);

}
