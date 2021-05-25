package it.gov.pagopa.tkm.annotation;

import org.yaml.snakeyaml.introspector.*;

import javax.validation.*;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Target({TYPE})
@Retention(RUNTIME)
@Constraint(validatedBy = CheckAtLeastOneNotNull.CheckAtLeastOneNotNullValidator.class)
@Documented
public @interface CheckAtLeastOneNotNull {

    String message() default "Validation failed: at least one required field is not present";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    String[] fieldNames();

    class CheckAtLeastOneNotNullValidator implements ConstraintValidator<CheckAtLeastOneNotNull, Object> {

        private String[] fieldNames;

        public void initialize(CheckAtLeastOneNotNull constraintAnnotation) {
            this.fieldNames = constraintAnnotation.fieldNames();
        }

        public boolean isValid(Object object, ConstraintValidatorContext constraintContext) {
            if (object == null) {
                return true;
            }
            try {
                for (String fieldName : fieldNames) {
                    PropertyUtils propertyUtils = new PropertyUtils();
                    Object property = propertyUtils.getProperty((Class<?>) object, fieldName);
                    if (property != null) {
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
