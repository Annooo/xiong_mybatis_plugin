package com.shong.xiong_mybatis_plugin.config;

import com.shong.xiong_mybatis_plugin.core.DynamicSqlEnable;
import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.type.AnnotationMetadata;

/**
 * @auther 10349 XIONGSY
 * @create 2021/8/4
 */
public class AutoConfigurationMapper implements ImportSelector {
    @Override
    public String[] selectImports(AnnotationMetadata importingClassMetadata) {
        return new String[]{DynamicSqlEnable.class.getName()};
    }
}
