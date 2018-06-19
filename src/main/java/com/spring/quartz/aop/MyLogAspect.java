package com.spring.quartz.aop;
import org.apache.log4j.Logger;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import com.spring.quartz.annotation.MyLog;

/**
 * @author sam
 * @since 2017/7/13
 */
@Aspect //AOP 切面
@Component
public class MyLogAspect {
	
	private final Logger logger = Logger.getLogger(MyLogAspect.class);

    //切入点
    @Pointcut(value = "@annotation(com.spring.quartz.annotation.MyLog)")
    private void pointcut() {

    }


    /**
     * 在方法执行前后
     * @param point
     * @param myLog
     * @return
     */
    @Around(value = "pointcut() && @annotation(myLog)")
    public Object around(ProceedingJoinPoint point, MyLog myLog) {
    	logger.info("++++执行了around方法++++");
        try {
            return point.proceed(); //执行程序
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            return throwable.getMessage();
        }
    }

    /**
     * 方法执行后
     * @param joinPoint
     * @param myLog
     * @param result
     * @return
     */
    @AfterReturning(value = "pointcut() && @annotation(myLog)", returning = "result")
    public Object afterReturning(JoinPoint joinPoint, MyLog myLog, Object result) {
    	logger.info("++++执行了afterReturning方法++++");
    	logger.info("执行结果：" + result);
        return result;
    }

    /**
     * 方法执行后 并抛出异常
     * @param joinPoint
     * @param myLog
     * @param ex
     */
    @AfterThrowing(value = "pointcut() && @annotation(myLog)", throwing = "ex")
    public void afterThrowing(JoinPoint joinPoint, MyLog myLog, Exception ex) {
    	logger.info("++++执行了afterThrowing方法++++");
    	logger.info("请求： 出现异常");
    }

}
