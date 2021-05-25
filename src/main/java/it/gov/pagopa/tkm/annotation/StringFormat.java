package it.gov.pagopa.tkm.annotation;

import java.lang.annotation.*;

@Target({ElementType.PARAMETER, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface StringFormat {
    StringFormatEnum value() default StringFormatEnum.NONE;
}