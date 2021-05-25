package it.gov.pagopa.tkm.jsondeserializer;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.std.*;

import java.io.*;

public class ToUpperCaseDeserializer extends StdDeserializer<String> {

    public ToUpperCaseDeserializer() {
        super(String.class);
    }

    @Override
    public String deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        return _parseString(p, ctxt).toUpperCase();
    }

}
