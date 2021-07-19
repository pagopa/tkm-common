package it.gov.pagopa.tkm.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BaseResultDetails {
    private boolean success = false;
    private String errorMessage;
}
