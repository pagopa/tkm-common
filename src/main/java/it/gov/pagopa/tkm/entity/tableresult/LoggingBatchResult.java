package it.gov.pagopa.tkm.entity.tableresult;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;

import javax.persistence.*;
import java.time.Instant;

@Log4j2
@Entity
@Table(name = "BATCH_RESULT")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoggingBatchResult {
    public static final int DETAILS_LEN = 10000;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID", unique = true, nullable = false)
    private Long id;

    @Column(name = "EXECUTION_TRACE_ID", nullable = false)
    private String executionTraceId;

    @Column(name = "TARGET_BATCH", nullable = false)
    private String targetBatch;

    @Column(name = "RUN_DATE", nullable = false)
    private Instant runDate;

    @Column(name = "RUN_OUTCOME", nullable = false)
    private boolean runOutcome;

    @Column(name = "RUN_DURATION_MILLIS", nullable = false)
    private long runDurationMillis;

    @Column(name = "DETAILS", nullable = false)
    private String details;

    @Column(name = "EXECUTED_BY")
    private String executedBy;

    @PreUpdate
    @PrePersist
    public void truncateDetails() {
        if (StringUtils.isNotBlank(details) && details.length() > DETAILS_LEN) {
            log.warn("Truncation TkmBatchResult.details because is too long. Char:" + details.length());
            details = StringUtils.truncate(details, DETAILS_LEN - 3) + "...";
        }
    }
}
