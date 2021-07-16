package it.gov.pagopa.tkm.model;

import lombok.Data;

@Data
public class BaseResultDetails {
    private boolean success;
    private String traceId;
}
