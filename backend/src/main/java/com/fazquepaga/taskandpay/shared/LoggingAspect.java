package com.fazquepaga.taskandpay.shared;

import java.util.Arrays;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
@ConditionalOnProperty(value = "app.logging.enabled", havingValue = "true", matchIfMissing = true)
public class LoggingAspect {

    @Pointcut(
            "within(com.fazquepaga.taskandpay..*) &&"
                    + " @within(org.springframework.web.bind.annotation.RestController)")
    public void controllerMethods() {}

    @Pointcut(
            "within(com.fazquepaga.taskandpay..*) &&"
                    + " @within(org.springframework.stereotype.Service)")
    public void serviceMethods() {}

    @Before("controllerMethods() || serviceMethods()")
    public void logBefore(JoinPoint joinPoint) {
        log.debug(
                "Enter: {}() with argument[s] = {}",
                joinPoint.getSignature().getName(),
                Arrays.toString(joinPoint.getArgs()));
    }

    @AfterReturning(pointcut = "controllerMethods() || serviceMethods()", returning = "result")
    public void logAfterReturning(JoinPoint joinPoint, Object result) {
        log.debug("Exit: {}() with result = {}", joinPoint.getSignature().getName(), result);
    }

    @AfterThrowing(pointcut = "controllerMethods() || serviceMethods()", throwing = "error")
    public void logAfterThrowing(JoinPoint joinPoint, Throwable error) {
        log.error(
                "Exception in {}() with cause = {}",
                joinPoint.getSignature().getName(),
                error.getCause() != null ? error.getCause() : "NULL");
    }
}
