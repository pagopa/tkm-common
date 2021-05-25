package it.gov.pagopa.tkm.formatter;

import org.springframework.format.Formatter;
import org.springframework.lang.*;

import java.util.Locale;

public class NoneFormatter implements Formatter<String> {

    @Override
    @NonNull
    public String parse(@NonNull String s, @NonNull Locale locale) {
        return s;
    }

    @Override
    @NonNull
    public String print(@NonNull String s, @NonNull Locale locale) {
        return s;
    }

}