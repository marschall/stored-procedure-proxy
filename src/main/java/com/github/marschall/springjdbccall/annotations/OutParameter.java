package com.github.marschall.springjdbccall.annotations;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Documented
@Retention(RUNTIME)
@Target(METHOD)
public @interface OutParameter {


  /**
   * The index of the out parameter.
   *
   * <p>If the out parameter isn't the last parameter you have to
   * provide the index of the out parameter.<p>
   *
   * @return the 1 based index of the out parameter
   */
  // TODO default index 1 or last
  // TODO 1 or 0 based
  int index() default -1;

  String name() default "";

  /**
   *
   * @return
   * @see java.sql.Types
   */
  int type() default Integer.MIN_VALUE;

}
