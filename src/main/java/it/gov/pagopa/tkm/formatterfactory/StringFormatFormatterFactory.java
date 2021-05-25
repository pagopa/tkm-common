package it.gov.pagopa.tkm.formatterfactory;

import it.gov.pagopa.tkm.annotation.StringFormat;
import it.gov.pagopa.tkm.formatter.LowerCaseFormatter;
import it.gov.pagopa.tkm.formatter.NoneFormatter;
import it.gov.pagopa.tkm.formatter.UpperCaseFormatter;
import org.springframework.context.support.EmbeddedValueResolutionSupport;
import org.springframework.format.AnnotationFormatterFactory;
import org.springframework.format.Formatter;
import org.springframework.format.Parser;
import org.springframework.format.Printer;
import org.springframework.lang.*;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class StringFormatFormatterFactory extends EmbeddedValueResolutionSupport implements AnnotationFormatterFactory<StringFormat> {

    private static final Set<Class<?>> FIELD_TYPES;

    static {
        Set<Class<?>> fieldTypes = new HashSet<>(1);
        fieldTypes.add(String.class);
        FIELD_TYPES = Collections.unmodifiableSet(fieldTypes);
    }

    @Override
    @NonNull
    public Set<Class<?>> getFieldTypes() {
        return FIELD_TYPES;
    }

    @Override
    @NonNull
    public Printer<?> getPrinter(@NonNull StringFormat annotation, @NonNull Class<?> fieldType) {
        return getFormatter(annotation);
    }

    @Override
    public Parser<?> getParser(@NonNull StringFormat annotation, @NonNull Class<?> fieldType) {
        return getFormatter(annotation);
    }

    private Formatter<String> getFormatter(StringFormat annotation) {
        switch (annotation.value()) {
            case UPPERCASE:
                return new UpperCaseFormatter();
            case LOWERCASE:
                return new LowerCaseFormatter();
            default:
                return new NoneFormatter();
        }
    }

}