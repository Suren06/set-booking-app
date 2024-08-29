package com.setbook.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
@Slf4j
public class LoggingAspect {

    @Pointcut("within(com.setbook.controller..*) || within(com.setbook.serviceImpl..*)")
    public void applicationPackagePointCut() {
        // Pointcut for service and controller layers
    }

    @Before("applicationPackagePointCut()")  // Ensure the correct method name here
    public void logBeforeMethod(JoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        log.info("Executing method: {} with arguments: {}", signature.getMethod().getName(), Arrays.toString(joinPoint.getArgs()));
    }

    @AfterReturning(value = "applicationPackagePointCut()", returning = "result")  // Ensure the correct method name here
    public void logAfterMethod(JoinPoint joinPoint, Object result) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        log.info("Method executed: {} with result: {}", signature.getMethod().getName(), result);
    }

    @AfterThrowing(value = "applicationPackagePointCut()", throwing = "exception")  // Ensure the correct method name here
    public void logAfterThrowing(JoinPoint joinPoint, Throwable exception) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        log.error("Exception in method: {} with message: {}", signature.getMethod().getName(), exception.getMessage());
    }

    @Around("applicationPackagePointCut()")
    public Object logAroundMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        long startTime = System.currentTimeMillis();
        try {
            Object result = joinPoint.proceed();
            long timeTaken = System.currentTimeMillis() - startTime;
            log.info("Method {} executed in {} ms", methodSignature.getMethod().getName(), timeTaken);
            return result;
        } catch (Throwable throwable) {
            long timeTaken = System.currentTimeMillis() - startTime;
            log.error("Method {} failed after {} ms with exception: {}", methodSignature.getMethod().getName(), timeTaken, throwable.getMessage());
            throw throwable;
        }
    }

}
