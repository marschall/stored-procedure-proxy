package com.github.marschall.storedprocedureproxy.procedures;

import java.math.BigDecimal;

import com.github.marschall.storedprocedureproxy.annotations.InOutParameter;
import com.github.marschall.storedprocedureproxy.annotations.Namespace;
import com.github.marschall.storedprocedureproxy.annotations.OutParameter;
import com.github.marschall.storedprocedureproxy.annotations.ProcedureName;
import com.github.marschall.storedprocedureproxy.annotations.ReturnValue;

@Namespace("SALES")
public interface DerbyProcedures {

  @ProcedureName("TOTAL_REVENUE")
  @OutParameter
  BigDecimal calculateRevenueByMonth(int month, int year);

  @ProcedureName("RAISE_PRICE")
  @InOutParameter
  BigDecimal raisePrice(BigDecimal price);

  @ProcedureName("TAX")
  @ReturnValue
  double salesTax(double subTotal);

}
