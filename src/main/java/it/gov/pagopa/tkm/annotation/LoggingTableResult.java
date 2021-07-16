package it.gov.pagopa.tkm.annotation;

import it.gov.pagopa.tkm.model.BaseResultDetails;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({METHOD})
@Retention(RUNTIME)
@Documented
public @interface LoggingTableResult {

    String batchName() default "";

    Class<? extends BaseResultDetails>[] resultClass();

}
