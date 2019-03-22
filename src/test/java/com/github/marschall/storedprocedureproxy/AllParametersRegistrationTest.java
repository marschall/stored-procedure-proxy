package com.github.marschall.storedprocedureproxy;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import com.github.marschall.storedprocedureproxy.ProcedureCallerFactory.ParameterRegistration;

@Retention(RUNTIME)
@Target(METHOD)
@ParameterizedTest
@EnumSource(ParameterRegistration.class)
public @interface AllParametersRegistrationTest {

}
