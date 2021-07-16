package it.gov.pagopa.tkm.aop.tableresult;


import it.gov.pagopa.tkm.annotation.LoggingTableResult;
import it.gov.pagopa.tkm.entity.tableresult.LoggingBatchResult;
import it.gov.pagopa.tkm.repository.tableresult.BatchResultRepository;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.sleuth.Span;
import org.springframework.cloud.sleuth.Tracer;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.Instant;

@Component
@Aspect
@Log4j2
public class AopLoggingTableResult {
    @Autowired
    private BatchResultRepository batchResultRepository;

    @Autowired
    private Tracer tracer;

    @Pointcut("@annotation(it.gov.pagopa.tkm.annotation.LoggingTableResult)")
    private void enableLoggingTableResult() {
    }

    @Around("enableLoggingTableResult()")
    public Object startEndLog(ProceedingJoinPoint joinPoint) throws Throwable {
        Instant start = Instant.now();
        Span span = tracer.currentSpan();
        String traceId = span != null ? span.context().traceId() : "noTraceId";
        LoggingTableResult declaredAnnotation = getLoggingTableResultAnnotation(joinPoint);
        String batchName = declaredAnnotation.batchName();
        String methodName = joinPoint.getSignature().getName();
        log.info(String.format("Start enableEnableLoggingTableResult %s", methodName));
        try {
            return joinPoint.proceed();
        } finally {
            LoggingBatchResult loggingBatchResult = LoggingBatchResult.builder()
                    .executionTraceId(traceId)
                    .runDate(start)
                    .runOutcome(false)
                    .targetBatch(StringUtils.firstNonBlank(batchName, methodName))
                    .runDurationMillis(Instant.now().toEpochMilli() - start.toEpochMilli())
                    .details(null)
                    .build();
           log.info(loggingBatchResult);
//            batchResultRepository.save(loggingBatchResult);
        }
    }

    private LoggingTableResult getLoggingTableResultAnnotation(ProceedingJoinPoint joinPoint) throws NoSuchMethodException {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = joinPoint.getTarget()
                .getClass()
                .getMethod(signature.getMethod().getName(),
                        signature.getMethod().getParameterTypes());
        return method.getDeclaredAnnotation(LoggingTableResult.class);
    }
}
