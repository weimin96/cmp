package com.wiblog.cmp.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author pwm
 * @date 2020/1/14
 */
// 运行时动态获取信息
@Retention(RetentionPolicy.RUNTIME)
// 作用在方法上
@Target(ElementType.METHOD)
public @interface CmpScheduled {

    String cron() default "";

    String des() default "";


}
