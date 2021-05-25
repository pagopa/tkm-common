package it.gov.pagopa.tkm.formatter;

import org.springframework.format.Formatter;

import java.util.Locale;

public class LowerCaseFormatter implements Formatter<String> {
    @Override
    public String parse(String s, Locale locale) {
        return s != null ? s.toLowerCase() : null;
    }

    @Override
    public String print(String s, Locale locale) {
        return s != null ? s.toLowerCase() : null;
    }
}