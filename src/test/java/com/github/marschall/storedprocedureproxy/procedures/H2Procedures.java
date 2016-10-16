package com.github.marschall.storedprocedureproxy.procedures;

import java.util.List;

import com.github.marschall.storedprocedureproxy.ValueExtractor;
import com.github.marschall.storedprocedureproxy.annotations.ReturnValue;

public interface H2Procedures {

  @ReturnValue
  String stringProcedure(String input);

  void voidProcedure(String input);

  @ReturnValue
  String noArgProcedure();

  List<IdName> simpleResultSet(ValueExtractor<IdName> extractor);

  public static final class IdName {

    private final long id;
    private final String name;

    public IdName(long id, String name) {
      this.id = id;
      this.name = name;
    }

    public long getId() {
      return this.id;
    }

    public String getName() {
      return this.name;
    }

  }

}
