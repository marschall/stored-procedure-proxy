package com.github.marschall.springjdbccall;

import com.github.marschall.springjdbccall.annotations.ReturnValue;

public interface H2Procedures {

  @ReturnValue
  String stringProcedure(String input);

  void voidProcedure(String input);

}
