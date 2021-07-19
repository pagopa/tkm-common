package it.gov.pagopa.tkm.annotation;

import it.gov.pagopa.tkm.config.BatchResultConfig;
import org.springframework.context.annotation.Import;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({TYPE})
@Retention(RUNTIME)
@Documented
@Import({BatchResultConfig.class})
public @interface EnableTkmLoggingTableResult {
}
