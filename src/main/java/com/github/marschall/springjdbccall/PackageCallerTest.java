package com.github.marschall.springjdbccall;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.github.marschall.springjdbccall.PackageCallerFactory.PackageCaller;

public class PackageCallerTest {

  @Test
  public void buildQualifiedCallString() {
    assertEquals("{call p.n()}", PackageCaller.buildQualifiedCallString("p", "n", 0));
    assertEquals("{call p.n(?)}", PackageCaller.buildQualifiedCallString("p", "n", 1));
    assertEquals("{call p.n(?,?)}", PackageCaller.buildQualifiedCallString("p", "n", 2));
  }

  @Test
  public void buildSimpleCallString() {
    assertEquals("{call n()}", PackageCaller.buildSimpleCallString("n", 0));
    assertEquals("{call n(?)}", PackageCaller.buildSimpleCallString("n", 1));
    assertEquals("{call n(?,?)}", PackageCaller.buildSimpleCallString("n", 2));
  }

}
