package it.gov.pagopa.tkm.repository.tableresult;

import it.gov.pagopa.tkm.entity.tableresult.LoggingBatchResult;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BatchResultRepository extends JpaRepository<LoggingBatchResult, Long> {

}
