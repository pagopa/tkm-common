package it.gov.pagopa.tkm.formatter;

import org.springframework.format.Formatter;

import java.text.ParseException;
import java.util.Locale;

public class UpperCaseFormatter implements Formatter<String> {
    @Override
    public String parse(String s, Locale locale) throws ParseException {
        return s != null ? s.toUpperCase() : null;
    }

    @Override
    public String print(String s, Locale locale) {
        return s != null ? s.toUpperCase() : null;
    }
}