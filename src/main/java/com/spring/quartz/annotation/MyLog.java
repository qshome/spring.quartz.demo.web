package com.spring.quartz.annotation;

import java.lang.annotation.*;

@Target({ElementType.METHOD})       // 注解使用对象
@Retention(RetentionPolicy.RUNTIME) // 注解使用时机
//@Documented
//@Inherited
public @interface MyLog {
	//String desc() default "";
}
