package it.gov.pagopa.tkm.annotation;

import it.gov.pagopa.tkm.aop.AopLogging;
import org.springframework.context.annotation.Import;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({TYPE})
@Retention(RUNTIME)
@Documented
@Import({AopLogging.class})
public @interface EnableTkmAopLogging {
}
