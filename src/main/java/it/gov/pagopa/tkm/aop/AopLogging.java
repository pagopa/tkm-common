package it.gov.pagopa.tkm.aop;


import lombok.extern.log4j.Log4j2;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.sleuth.Span;
import org.springframework.cloud.sleuth.Tracer;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;

@Aspect
@Log4j2
public class AopLogging {
    @Autowired
    private Tracer tracer;

    @Pointcut("@annotation(it.gov.pagopa.tkm.annotation.EnableExecutionTime)")
    private void enableExecutionTimeAnnotation() {
    }

    @Pointcut("within(@it.gov.pagopa.tkm.annotation.EnableStartEndLogging *) || @annotation(it.gov.pagopa.tkm.annotation.EnableStartEndLogging)")
    private void enableStartEndLogAnnotation() {
    }

    @Around("enableExecutionTimeAnnotation()")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        Span span = tracer.nextSpan(tracer.currentSpan()).start();
        Tracer.SpanInScope ws = this.tracer.withSpan(span.start());
        Instant start = Instant.now();
        try {
            return joinPoint.proceed();
        } catch (Throwable ex) {
            span.error(ex);
            throw ex;
        } finally {
            Instant finish = Instant.now();
            long timeElapsed = Duration.between(start, finish).toMillis();
            log.info(String.format("Method %s executed in %s ms", joinPoint.getSignature().getName(), timeElapsed));
            ws.close();
            span.end();
        }
    }

    @Around("enableStartEndLogAnnotation()")
    public Object startEndLog(ProceedingJoinPoint joinPoint) throws Throwable {
        Span span = tracer.nextSpan(tracer.currentSpan()).start();
        Tracer.SpanInScope ws = this.tracer.withSpan(span.start());
        String methodName = joinPoint.getSignature().getName();
        log.info(String.format("Start Method %s", methodName));
        log.trace("Method args: " + Arrays.toString(joinPoint.getArgs()));
        try {
            return joinPoint.proceed();
        } catch (Throwable ex) {
            span.error(ex);
            throw ex;
        } finally {
            log.info(String.format("End Method %s", methodName));
            ws.close();
            span.end();
        }
    }
}
