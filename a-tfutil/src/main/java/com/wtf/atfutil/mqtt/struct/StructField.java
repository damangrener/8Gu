package cn.ac.iscas.util.struct;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface StructField {
    int order();
}
