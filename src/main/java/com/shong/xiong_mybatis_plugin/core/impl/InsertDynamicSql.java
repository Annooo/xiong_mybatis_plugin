package com.shong.xiong_mybatis_plugin.core.impl;

import com.shong.xiong_mybatis_plugin.core.AbstractDynamicSql;
import org.apache.ibatis.mapping.ResultMap;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.scripting.defaults.RawSqlSource;
import org.apache.ibatis.scripting.xmltags.MixedSqlNode;
import org.apache.ibatis.scripting.xmltags.SqlNode;
import org.apache.ibatis.scripting.xmltags.StaticTextSqlNode;
import org.apache.ibatis.session.SqlSessionFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * @auther 10349 XIONGSY
 * @create 2021/8/4
 */
public class InsertDynamicSql extends AbstractDynamicSql {

    @Override
    public String getId(Class intfClass) {
        return intfClass.getCanonicalName() + ".dynamicInsert";
    }

    @Override
    public SqlCommandType getSqlCommandType() {
        return SqlCommandType.INSERT;
    }

    public StringBuffer handlerSql(Class clazz) {
        StringBuffer sql = new StringBuffer("insert into ");
        sql.append(getTableName(clazz));
        sql.append(getTableColumns(clazz));
        sql.append(" values");
        sql.append(getTableFields(clazz));
        return sql;
    }

    public SqlSource sqlSource(SqlSessionFactory sqlSessionFactory, Class genericClazz) {
        StringBuffer sb = handlerSql(genericClazz);
        List<SqlNode> contents = new ArrayList<>();
        contents.add(new StaticTextSqlNode(sb.toString()));
        MixedSqlNode mixedSqlNode = new MixedSqlNode(contents);
        SqlSource sqlSource = new RawSqlSource(sqlSessionFactory.getConfiguration(), mixedSqlNode, genericClazz);
        return sqlSource;
    }

    @Override
    public List<ResultMap> getResultMaps(SqlSessionFactory sqlSessionFactory,Class genericClazz, Class<?> intfClass) {
        return new ArrayList<>();
    }
}
