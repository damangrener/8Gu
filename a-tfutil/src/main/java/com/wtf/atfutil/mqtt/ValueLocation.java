package cn.ac.iscas.util;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 字符串和对象转换，没有的位置补0
 * @author WTF
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ValueLocation {
    int begin();

    int end();

    String format() default "";

    String timezone() default "GMT+8";
}