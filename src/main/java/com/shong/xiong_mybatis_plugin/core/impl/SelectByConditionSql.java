package com.shong.xiong_mybatis_plugin.core.impl;

import com.shong.xiong_mybatis_plugin.core.AbstractDynamicSql;
import org.apache.ibatis.mapping.ResultMap;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.scripting.xmltags.*;
import org.apache.ibatis.session.SqlSessionFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @auther 10349 XIONGSY
 * @create 2021/8/4
 */
public class SelectByConditionSql extends AbstractDynamicSql {
    @Override
    public String getId(Class intfClass) {
        return intfClass.getCanonicalName() + ".selectByCondition";
    }

    @Override
    public SqlCommandType getSqlCommandType() {
        return SqlCommandType.SELECT;
    }

    public StringBuffer handlerSql(Class clazz) {
        StringBuffer sql = new StringBuffer("select " + getQTableColumns(clazz) + " from ");
        sql.append(getTableName(clazz));
        return sql;
    }

    /**
    select * from xx
     <if test="sc!=null and sc.conditionSql!=null">
     ${sc.conditionSql}
     <if/>
    */
    @Override
    public SqlSource sqlSource(SqlSessionFactory sqlSessionFactory, Class genericClazz) {
        StringBuffer sb = handlerSql(genericClazz);
        List<SqlNode> contents = new ArrayList<>();
        contents.add(new StaticTextSqlNode(sb.toString()));
        contents.add(getIfSqlNode(sqlSessionFactory,genericClazz));
        MixedSqlNode mixedSqlNode = new MixedSqlNode(contents);
        SqlSource sqlSource = new DynamicSqlSource(sqlSessionFactory.getConfiguration(), mixedSqlNode);
        return sqlSource;
    }

    private SqlNode getIfSqlNode(SqlSessionFactory sqlSessionFactory, Class genericClazz) {
        List<SqlNode> ifChildContents = new ArrayList<>();
        ifChildContents.add(new TextSqlNode("${sc.conditionSql}"));
        MixedSqlNode ifMixedSqlNode = new MixedSqlNode(ifChildContents);
        IfSqlNode ifSqlNode = new IfSqlNode(ifMixedSqlNode, "sc!=null and sc.conditionSql!=null");
        return ifSqlNode;
    }

    @Override
    public List<ResultMap> getResultMaps(SqlSessionFactory sqlSessionFactory, Class genericClazz, Class<?> intfClass) {
        String id = getResultId(genericClazz, intfClass);
        ResultMap resultMap = sqlSessionFactory.getConfiguration().getResultMap(id);
        return Collections.singletonList(resultMap);
    }
}
