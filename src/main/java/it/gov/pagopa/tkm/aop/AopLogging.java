package it.gov.pagopa.tkm.aop;


import lombok.extern.log4j.Log4j2;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;

@Component
@Aspect
@Log4j2
public class AopLogging {

    @Pointcut("within(@it.gov.pagopa.tkm.annotation.EnableExecutionTime *) || @annotation(it.gov.pagopa.tkm.annotation.EnableExecutionTime)")
    private void enableExecutionTimeAnnotation() {
    }

    @Pointcut("within(@it.gov.pagopa.tkm.annotation.EnableStartEndLogging *) || @annotation(it.gov.pagopa.tkm.annotation.EnableStartEndLogging)")
    private void enableStartEndLogAnnotation() {
    }

    @Around("enableExecutionTimeAnnotation()")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        Instant start = Instant.now();
        try {
            return joinPoint.proceed();
        } finally {
            Instant finish = Instant.now();
            long timeElapsed = Duration.between(start, finish).toMillis();
            log.info(String.format("Method %s executed in %s ms", joinPoint.getSignature().getName(), timeElapsed));
        }
    }

    @Around("enableStartEndLogAnnotation()")
    public Object startEndLog(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        log.info(String.format("Start Method %s", methodName));
        log.trace("Method args: " + Arrays.toString(joinPoint.getArgs()));
        try {
            return joinPoint.proceed();
        } finally {
            log.info(String.format("End Method %s", methodName));
        }
    }
}
