package com.ally.blogapp.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class PerformanceAspect {

    private static final Logger log = LoggerFactory.getLogger(PerformanceAspect.class);
    private static final long SLOW_THRESHOLD_MS = 500;

    @Pointcut("execution(* com.ally.blogapp.service.*.*(..))")
    public void serviceLayer() {}

    @Around("serviceLayer()")
    public Object measureExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        try {
            return joinPoint.proceed();
        } finally {
            long elapsed = System.currentTimeMillis() - start;
            String method = joinPoint.getSignature().getDeclaringTypeName()
                    + "." + joinPoint.getSignature().getName();
            if (elapsed > SLOW_THRESHOLD_MS) {
                log.warn("SLOW [{}ms] {}", elapsed, method);
            } else {
                log.debug("PERF [{}ms] {}", elapsed, method);
            }
        }
    }
}
