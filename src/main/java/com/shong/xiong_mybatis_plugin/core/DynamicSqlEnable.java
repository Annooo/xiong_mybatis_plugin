package com.shong.xiong_mybatis_plugin.core;

import com.shong.xiong_mybatis_plugin.annotation.EnableMapper;
import com.shong.xiong_mybatis_plugin.base.BaseDao;
import org.apache.ibatis.io.ResolverUtil;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.mapper.MapperScannerConfigurer;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ImportAware;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.ReflectionUtils;
import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ServiceLoader;
import java.util.Set;

/**
 * @auther 10349 XIONGSY
 * @create 2021/8/4
 */
public class DynamicSqlEnable implements InitializingBean, ImportAware {

    private Boolean isLoad = false;

    @Autowired
    private SqlSessionFactory sqlSessionFactory;

    @Autowired
    private MapperScannerConfigurer mapperScannerConfigurer;

    private ResourceLoader resourceLoader;

    public void enable(SqlSessionFactory sqlSessionFactory, String packageName) {
        scanClass(sqlSessionFactory, packageName);
    }

    private void scanClass(SqlSessionFactory sqlSessionFactory, String packageName) {
        ResolverUtil<Class<?>> resolverUtil = new ResolverUtil<>();
        resolverUtil.find(new ResolverUtil.IsA(BaseDao.class), packageName);
        Set<Class<? extends Class<?>>> handlerSet = resolverUtil.getClasses();
        ServiceLoader<DynamicSql> load = ServiceLoader.load(DynamicSql.class);
        for (Class<? extends Class<?>> aClass : handlerSet) {
            if (aClass.getSimpleName().equalsIgnoreCase("BaseDao")) {
                continue;
            }
            Class genericType = handlerIntfGenericType(aClass);
            for (DynamicSql dynamicSql : load) {
                dynamicSql.handler(sqlSessionFactory, genericType, aClass);
            }
        }
    }

    private Class handlerIntfGenericType(Class clazz) {
        Type[] genericInterfaces = clazz.getGenericInterfaces();
        for (Type genericInterface : genericInterfaces) {
            if (((ParameterizedTypeImpl) genericInterface).getRawType().isAssignableFrom(BaseDao.class)) {
                ParameterizedType parameterizedType = (ParameterizedType) genericInterface;
                Type[] types = parameterizedType.getActualTypeArguments();
                return (Class<?>) types[0];
            }
        }
        return null;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if(isLoad) {
            return;
        }
        try {
            Field basePackage = ReflectionUtils.findField(mapperScannerConfigurer.getClass(), "basePackage");
            basePackage.setAccessible(true);
            String basePackageStr = (String) basePackage.get(mapperScannerConfigurer);
            enable(sqlSessionFactory, basePackageStr);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setImportMetadata(AnnotationMetadata importMetadata) {
        AnnotationAttributes comArr = AnnotationAttributes.fromMap(importMetadata.getAnnotationAttributes(EnableMapper.class.getName(), false));
        String[] basePackages = comArr.getStringArray("basePackages");
        if (basePackages.length > 0) {
            for (String basePackage : basePackages) {
                enable(sqlSessionFactory, basePackage);
            }
        } else {
            try {
                Field basePackage = ReflectionUtils.findField(mapperScannerConfigurer.getClass(), "basePackage");
                basePackage.setAccessible(true);
                String basePackageStr = (String) basePackage.get(mapperScannerConfigurer);
                enable(sqlSessionFactory, basePackageStr);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        isLoad = true;
    }
}
