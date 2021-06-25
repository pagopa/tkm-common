package it.gov.pagopa.tkm.annotation;

import javax.validation.*;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.*;

@Target({TYPE})
@Retention(RUNTIME)
@Constraint(validatedBy = CheckAtLeastOneNotEmpty.CheckAtLeastOneNotEmptyValidator.class)
@Documented
public @interface CheckAtLeastOneNotEmpty {

    String message() default "Validation failed: at least one required field is not present";

    String[] fieldNames();

    class CheckAtLeastOneNotEmptyValidator implements ConstraintValidator<CheckAtLeastOneNotEmpty, Object> {

        private String[] fieldNames;

        @Override
        public void initialize(CheckAtLeastOneNotEmpty constraintAnnotation) {
            this.fieldNames = constraintAnnotation.fieldNames();
        }

        public boolean isValid(Object object, ConstraintValidatorContext constraintContext) {
            if (object == null) {
                return true;
            }
            try {
                for (String fieldName : fieldNames) {
                    Object property = PropertyUtils.getProperty(object, fieldName);
                    if (property instanceof String && StringUtils.isNotBlank((String) property)
                            || !(property instanceof String) && property != null) {
                        return true;
                    }
                }
                return false;
            } catch (Exception e) {
                return false;
            }
        }

    }
}
