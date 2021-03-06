package it.gov.pagopa.tkm.aop.tableresult;


import com.fasterxml.jackson.databind.ObjectMapper;
import it.gov.pagopa.tkm.annotation.LoggingTableResult;
import it.gov.pagopa.tkm.entity.tableresult.LoggingBatchResult;
import it.gov.pagopa.tkm.model.BaseResultDetails;
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

import java.lang.reflect.Method;
import java.time.Instant;

@Aspect
@Log4j2
public class AopLoggingTableResult {
    @Autowired
    private BatchResultRepository batchResultRepository;

    @Autowired
    private Tracer tracer;

    @Autowired
    private ObjectMapper objectMapper;

    @Pointcut("@annotation(it.gov.pagopa.tkm.annotation.LoggingTableResult)")
    private void enableLoggingTableResult() {
        //Do nothing because is a part of SPRING AOP
    }

    @Around("enableLoggingTableResult()")
    public Object saveResultOnTable(ProceedingJoinPoint joinPoint) throws Throwable {
        Instant start = Instant.now();
        Span span = tracer.nextSpan(tracer.currentSpan()).start();
        Tracer.SpanInScope ws = this.tracer.withSpan(span.start());
        LoggingTableResult declaredAnnotation = getLoggingTableResultAnnotation(joinPoint);
        String signatureName = joinPoint.getSignature().getName();
        log.debug(String.format("Start saveResultOnTable %s", signatureName));
        Object proceed = null;
        try {
            span.tag("class", joinPoint.getTarget().getClass().getSimpleName());
            span.tag("method", signatureName);
            proceed = joinPoint.proceed();
            return proceed;
        } catch (Throwable ex) {
            span.error(ex);
            throw ex;
        } finally {
            saveOnTable(start, declaredAnnotation, signatureName, span, proceed);
            ws.close();
            span.end();
        }
    }

    private void saveOnTable(Instant start, LoggingTableResult loggingTableResult, String signatureName, Span span, Object proceed) {
        try {
            if (proceed == null || !BaseResultDetails.class.isAssignableFrom(proceed.getClass())) {
                log.error("Invalid table result method return " + (proceed != null ? proceed.getClass() : null));
                if (loggingTableResult.strict()) {
                    return;
                } else {
                    proceed = new BaseResultDetails();
                    log.warn("Proceed with empty details");
                }
            }
            BaseResultDetails baseResultDetails = (BaseResultDetails) proceed;
            String traceId = span.context().traceId();

            String targetBatch = StringUtils.firstNonBlank(loggingTableResult.batchName(), signatureName);
            String executedBy = getExecutedBy();
            LoggingBatchResult loggingBatchResult = LoggingBatchResult.builder()
                    .executionTraceId(traceId)
                    .runDate(start)
                    .runOutcome(baseResultDetails.isSuccess())
                    .details(objectMapper.writeValueAsString(baseResultDetails))
                    .targetBatch(targetBatch)
                    .runDurationMillis(Instant.now().toEpochMilli() - start.toEpochMilli())
                    .executedBy(executedBy)
                    .build();
            batchResultRepository.save(loggingBatchResult);
            log.debug(loggingBatchResult);
        } catch (Exception e) {
            log.error(e);
        }
    }

    private String getExecutedBy() {
        String executedBy = StringUtils.firstNonBlank(System.getenv("HOSTNAME"), System.getenv("COMPUTERNAME"));
        if(StringUtils.isBlank(executedBy)){
            log.warn("executedBy cannot be resolved. Please set HOSTNAME or COMPUTERNAME");
        }
        return executedBy;
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
