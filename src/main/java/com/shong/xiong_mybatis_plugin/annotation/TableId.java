package com.shong.xiong_mybatis_plugin.annotation;


import com.shong.xiong_mybatis_plugin.constants.IdType;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface TableId {
    String value() default "";

    IdType type() default IdType.NONE;
}
