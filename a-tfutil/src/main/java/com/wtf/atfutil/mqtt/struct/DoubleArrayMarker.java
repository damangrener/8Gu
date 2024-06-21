package cn.ac.iscas.util.struct;

import java.lang.annotation.*;

/**
 * @author WTF
 * @date 2022/8/10 16:00
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface DoubleArrayMarker {

    /**
     * 行
     */
    int row();

    /**
     * 列
     */
    int column();

}
