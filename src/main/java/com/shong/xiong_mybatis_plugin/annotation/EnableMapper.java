package com.shong.xiong_mybatis_plugin.annotation;

import com.shong.xiong_mybatis_plugin.config.AutoConfigurationMapper;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Import(AutoConfigurationMapper.class)
public @interface EnableMapper {
    String[] basePackages() default {};
}
