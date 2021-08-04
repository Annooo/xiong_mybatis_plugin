package com.shong.xiong_mybatis_plugin.core.impl;

import com.shong.xiong_mybatis_plugin.annotation.Ignore;
import com.shong.xiong_mybatis_plugin.annotation.TableId;
import com.shong.xiong_mybatis_plugin.core.AbstractDynamicSql;
import org.apache.ibatis.mapping.ResultMap;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.scripting.xmltags.*;
import org.apache.ibatis.session.SqlSessionFactory;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * @auther 10349 XIONGSY
 * @create 2021/8/4
 */
public class UpdateDynamicSql extends AbstractDynamicSql {
    @Override
    public String getId(Class intfClass) {
        return intfClass.getCanonicalName() + ".dynamicUpdate";
    }

    @Override
    public SqlCommandType getSqlCommandType() {
        return SqlCommandType.UPDATE;
    }

    private String getContent(Field field) {
        return getColumnName(field) + " = #{" + field.getName() + "},";
    }

    private SqlNode getTrimSqlNode(SqlSessionFactory sqlSessionFactory,Class clazz) {
        SqlNode sqlNode = handlerIfMixedSqlNode(clazz);
        return new TrimSqlNode(sqlSessionFactory.getConfiguration(), sqlNode, "SET", null, null, ",");
    }

    public SqlNode handlerIfMixedSqlNode(Class clazz) {
        List<SqlNode> contents = new ArrayList<>();
        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(Ignore.class)) {
                continue;
            }
            List<SqlNode> ifChildContents = new ArrayList<>();
            ifChildContents.add(new StaticTextSqlNode(getContent(field)));
            MixedSqlNode ifMixedSqlNode = new MixedSqlNode(ifChildContents);
            IfSqlNode ifSqlNode = new IfSqlNode(ifMixedSqlNode, field.getName() + " != null and " + field.getName() + " != ''");
            contents.add(ifSqlNode);
        }
        return new MixedSqlNode(contents);
    }

    public StringBuffer handlerSql(Class clazz) {
        StringBuffer sql = new StringBuffer("update " + getTableName(clazz) + " ");
        return sql;
    }

    private String getWhereSql(Class clazz) {
        String sql = "";
        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(Ignore.class)) {
                continue;
            }
            if(field.isAnnotationPresent(TableId.class)) {
                sql = " where " + field.getAnnotation(TableId.class).value() + " = #{" + field.getName() + "}";
                break;
            }
        }
        return sql;
    }

    @Override
    public SqlSource sqlSource(SqlSessionFactory sqlSessionFactory, Class genericClazz) {
        StringBuffer sb = handlerSql(genericClazz);
        List<SqlNode> contents = new ArrayList<>();
        contents.add(new StaticTextSqlNode(sb.toString()));
        contents.add(getTrimSqlNode(sqlSessionFactory,genericClazz));
        contents.add(new StaticTextSqlNode(getWhereSql(genericClazz)));
        MixedSqlNode mixedSqlNode = new MixedSqlNode(contents);
        SqlSource sqlSource = new DynamicSqlSource(sqlSessionFactory.getConfiguration(), mixedSqlNode);
        return sqlSource;
    }

    @Override
    public List<ResultMap> getResultMaps(SqlSessionFactory sqlSessionFactory, Class genericClazz, Class<?> intfClass) {
        return new ArrayList<>();
    }
}
