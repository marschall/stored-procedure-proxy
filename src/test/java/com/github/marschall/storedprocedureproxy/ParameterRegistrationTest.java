package com.github.marschall.storedprocedureproxy;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.junit.jupiter.params.ParameterizedTest;

@Retention(RUNTIME)
@Target(METHOD)
@ParameterizedTest
@IndexRegistrationParameters
public @interface ParameterRegistrationTest {

}
