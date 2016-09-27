package com.github.marschall.storedprocedureproxy;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Test;

public class PostgresDatabasePopulatorTest {

  @Test
  public void split() {
    assertEquals(Arrays.asList("aaxx", "bb"), PostgresDatabasePopulator.split("aaxxbb", "xx"));
    assertEquals(Arrays.asList("aaxx", "bbxx"), PostgresDatabasePopulator.split("aaxxbbxx", "xx"));

    assertEquals(Collections.singletonList("aaxx"), PostgresDatabasePopulator.split("aaxx", "xx"));

    assertEquals(Collections.singletonList("aaxx"), PostgresDatabasePopulator.split("aaxx", "xxx"));
  }

}
