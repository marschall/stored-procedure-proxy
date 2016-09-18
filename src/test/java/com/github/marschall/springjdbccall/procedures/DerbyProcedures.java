package com.github.marschall.springjdbccall.procedures;

import java.math.BigDecimal;

import com.github.marschall.springjdbccall.annotations.Namespace;
import com.github.marschall.springjdbccall.annotations.OutParameter;
import com.github.marschall.springjdbccall.annotations.ProcedureName;
import com.github.marschall.springjdbccall.annotations.ReturnValue;

@Namespace("SALES")
public interface DerbyProcedures {

  @ProcedureName("TOTAL_REVENUE")
  @OutParameter
  BigDecimal calculateRevenueByMonth(int month, int year);

  @ProcedureName("TAX")
  @ReturnValue
  double salesTax(double subTotal);

}
