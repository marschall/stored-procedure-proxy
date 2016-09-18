package com.github.marschall.springjdbccall;

import java.math.BigDecimal;

public class DerbyProcedureSources {

  public static void calculateRevenueByMonth(int month, int year, BigDecimal[] out) {
    out[0] = new BigDecimal(year * 100 + month);
  }

  public static double tax(double subTotal) {
    return subTotal * 0.06d;
  }

}
