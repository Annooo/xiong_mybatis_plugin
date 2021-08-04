package com.shong.xiong_mybatis_plugin.condition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @auther 10349 XIONGSY
 * @create 2021/8/4
 */
public class SqlCondition {

    //存储字段和值的映射
    private Map<String, Object> conditionColumnMap = new HashMap<>();
    private String paramName = "PARAMNAME_";
    private AtomicInteger paramIndex = new AtomicInteger();
    private String prefix = "#{sc.conditionColumnMap." + paramName;
    private String subfix = "}";
    private String and = " AND ";
    private String or = " OR ";
    private String orderBy = " ORDER BY ";

    //存储条件sql片段
    private List<String> where = new ArrayList<>();
    private List<String> groupby = new ArrayList<>();
    private List<String> having = new ArrayList<>();
    private Map<String, String> orderby = new HashMap<>();

    public SqlCondition eq(String columnName, Object params) {
        getSqlSeg(columnName, params, " = ");
        return this;
    }

    public SqlCondition ge(String columnName, Object params) {
        getSqlSeg(columnName, params, " >= ");
        return this;
    }

    public SqlCondition le(String columnName, Object params) {
        getSqlSeg(columnName, params, " <= ");
        return this;
    }

    public SqlCondition ne(String columnName, Object params) {
        getSqlSeg(columnName, params, " <> ");
        return this;
    }

    public SqlCondition exists(String sql) {
        where.add("EXISTS (" + sql + ")");
        return this;
    }

    public SqlCondition groupBy(String columns) {
        groupby.add(" GROUP BY " + columns);
        return this;
    }

    public SqlCondition having(String sql) {
        having.add(" HAVING " + sql);
        return this;
    }

    /**
     * areaCode desc areaName asc
     *
     * @param sql
     * @param orderType
     * @return
     */
    public SqlCondition orderBy(String sql, String orderType) {
        orderby.put(sql, orderType);
        return this;
    }

    public SqlCondition and() {
        where.add(and);
        return this;
    }

    public SqlCondition or() {
        where.add(or);
        return this;
    }

    private String getSqlSeg(String columnName, Object params, String op) {
        int i = paramIndex.incrementAndGet();
        String sqlSeg = columnName + op + prefix + i + subfix;
        where.add(sqlSeg);
        conditionColumnMap.put(paramName + i, params);
        return sqlSeg;
    }

    /**
     * 拼接的 IfSqlNode 节点中test条件和#{sc.conditionSql} ，就会调用参数的属性 conditionSql 对应的get方法，就会走到这里，替换掉#{sc.conditionSql} 为SqlCondition实体拼接的sql语句
     *
     * @return
     */
    public String getConditionSql() {
        StringBuffer sqlSeg = new StringBuffer();
        whereSql(sqlSeg);
        groupBySql(sqlSeg);
        havingSql(sqlSeg);
        orderBySql(sqlSeg);
        return sqlSeg.toString();
    }

    private void whereSql(StringBuffer sqlSeg) {
        if (where.size() > 0) {
            sqlSeg.append(" where ");
            for (String s : where) {
                sqlSeg.append(s);
            }
        }
    }

    private void groupBySql(StringBuffer sqlSeg) {
        if (groupby.size() > 0) {
            for (String s : groupby) {
                sqlSeg.append(s);
            }
        }
    }

    private void havingSql(StringBuffer sqlSeg) {
        if (having.size() > 0) {
            for (String s : having) {
                sqlSeg.append(s);
            }
        }
    }

    private void orderBySql(StringBuffer sqlSeg) {
        if (orderby.size() > 0) {
            // 拼接上orderby语句
            sqlSeg.append(orderBy);
            for (String column : orderby.keySet()) {
                sqlSeg.append(column);
                sqlSeg.append(" ");
                sqlSeg.append(orderby.get(column));
                sqlSeg.append(" ");
                sqlSeg.append(",");
            }
            sqlSeg.delete(sqlSeg.length() - 1, sqlSeg.length());
        }
    }
}
