package edu.itc.shop.service;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class TimingAspect {

    private static final Logger log =
            LoggerFactory.getLogger(TimingAspect.class);

    @Around("execution(public * edu.itc.shop.service..*(..))")
    public Object timeServiceMethod(ProceedingJoinPoint joinPoint)
            throws Throwable {

        long start = System.nanoTime();

        try {
            return joinPoint.proceed();
        } finally {
            long elapsedMicros =
                    (System.nanoTime() - start) / 1_000;

            log.debug("{} took {} µs",
                    joinPoint.getSignature().toShortString(),
                    elapsedMicros);
        }
    }
}