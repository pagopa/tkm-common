package it.gov.pagopa.tkm.formatter;

import org.springframework.format.Formatter;

import java.text.ParseException;
import java.util.Locale;

public class NoneFormatter implements Formatter<String> {
    @Override
    public String parse(String s, Locale locale) throws ParseException {
        return s;
    }

    @Override
    public String print(String s, Locale locale) {
        return s;
    }
}