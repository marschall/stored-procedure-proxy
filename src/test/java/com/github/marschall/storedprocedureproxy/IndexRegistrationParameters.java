package com.github.marschall.storedprocedureproxy;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.junit.jupiter.params.provider.EnumSource;

import com.github.marschall.storedprocedureproxy.ProcedureCallerFactory.ParameterRegistration;

@Documented
@Retention(RUNTIME)
@Target(METHOD)
@EnumSource(value = ParameterRegistration.class, names = {"INDEX_ONLY", "INDEX_AND_TYPE"})
public @interface IndexRegistrationParameters {

}
