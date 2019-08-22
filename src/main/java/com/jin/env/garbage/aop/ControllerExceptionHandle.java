package com.jin.env.garbage.aop;

import com.jin.env.garbage.utils.Constants;
import com.jin.env.garbage.utils.ResponseData;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.sql.SQLException;

@Component
@Aspect
public class ControllerExceptionHandle {

    private Logger logger = LoggerFactory.getLogger(ControllerExceptionHandle.class);

    @Pointcut("execution(* com.jin.env.garbage.controller.*.*.*(..))")
    public void init(){
        logger.info("aop切入点");
    }


    @Around(value = "init()")
    public Object test_round(ProceedingJoinPoint joinPoint){
        Object proceed = null;
        try {
            proceed = joinPoint.proceed();
        } catch (Throwable throwable) {
            ResponseData responseData = new ResponseData();
            throwable.printStackTrace();
            if (throwable instanceof NumberFormatException){
                responseData.setMsg("数字转换异常");
            } else if (throwable instanceof NullPointerException){
                responseData.setMsg("空指针异常");
            } else if (throwable instanceof RuntimeException){
                responseData.setMsg(throwable.getMessage());
            } else{
                responseData.setMsg(throwable.getMessage());
            }
            logger.error("异常日志", throwable);
            responseData.setStatus(Constants.responseStatus.Failure.getStatus());
            return responseData;
        }
        return proceed;
    }
}
