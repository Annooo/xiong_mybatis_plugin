package com.shong.xiong_mybatis_plugin.core;

import org.apache.ibatis.session.SqlSessionFactory;

/**
 * @auther 10349 XIONGSY
 * @create 2021/8/4
 */
public interface DynamicSql {
    /**
     * @param sqlSessionFactory
     * @param genericClazz      mapper泛型中实体class
     * @param mapperClass       mapper接口的class
     */
    void handler(SqlSessionFactory sqlSessionFactory, Class genericClazz, Class<?> mapperClass);
}
