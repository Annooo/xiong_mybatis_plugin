package com.shong.xiong_mybatis_plugin.core;

import com.shong.xiong_mybatis_plugin.annotation.Ignore;
import com.shong.xiong_mybatis_plugin.annotation.TableField;
import com.shong.xiong_mybatis_plugin.annotation.TableId;
import com.shong.xiong_mybatis_plugin.annotation.TableName;
import com.shong.xiong_mybatis_plugin.constants.IdType;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.executor.keygen.Jdbc3KeyGenerator;
import org.apache.ibatis.executor.keygen.NoKeyGenerator;
import org.apache.ibatis.mapping.*;
import org.apache.ibatis.reflection.MetaClass;
import org.apache.ibatis.session.SqlSessionFactory;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

/**
 * @auther 10349 XIONGSY
 * @create 2021/8/4
 */
public abstract class AbstractDynamicSql implements DynamicSql {

    @Override
    public void handler(SqlSessionFactory sqlSessionFactory, Class genericClazz, Class<?> mapperClass) {
        // 初始化mapper的resultMap
        if (!sqlSessionFactory.getConfiguration().hasResultMap(getResultId(genericClazz, mapperClass))) {
            resultMap(sqlSessionFactory, genericClazz, mapperClass);
        }
        // 初始化mapper的 MappedStatement
        mappedStatement(sqlSessionFactory, genericClazz, mapperClass);
        if (!sqlSessionFactory.getConfiguration().hasMapper(mapperClass)) {
            sqlSessionFactory.getConfiguration().addMapper(mapperClass);
        }
    }

    public String getResultId(Class genericClazz, Class<?> mapperClass) {
        return mapperClass.getCanonicalName() + "." + genericClazz.getSimpleName() + "dynamic";
    }

    /**
     * mapper中，每个指定方法都创建一个MappedStatement
     *
     * @param sqlSessionFactory
     * @param genericClazz
     * @param mapperClass
     * @return
     */
    public MappedStatement mappedStatement(SqlSessionFactory sqlSessionFactory, Class genericClazz, Class<?> mapperClass) {
        boolean isAuto = false;
        String keyProperty = "";
        for (Field field : genericClazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(TableId.class)) {
                TableId annotation = field.getAnnotation(TableId.class);
                IdType type = annotation.type();
                isAuto = type == IdType.AUTO;
                keyProperty = field.getName();
                break;
            }
        }
        MappedStatement.Builder statementBuilder = new MappedStatement.Builder(sqlSessionFactory.getConfiguration(), getId(mapperClass), sqlSource(sqlSessionFactory, genericClazz), getSqlCommandType())
                .resource(null)
                .fetchSize(null)
                .timeout(null)
                .statementType(StatementType.PREPARED)
                .keyGenerator(isAuto ? Jdbc3KeyGenerator.INSTANCE : NoKeyGenerator.INSTANCE)
                .keyProperty(isAuto ? keyProperty : null)
                .keyColumn(null)
                .databaseId(null)
                .lang(sqlSessionFactory.getConfiguration().getLanguageDriver(null))
                .resultOrdered(false)
                .resultSets(null)
                .resultMaps(getResultMaps(sqlSessionFactory, genericClazz, mapperClass))
                .resultSetType(null)
                .flushCacheRequired(true)
                .useCache(false)
                .cache(null);
        MappedStatement statement = statementBuilder.build();
        sqlSessionFactory.getConfiguration().addMappedStatement(statement);
        return statement;
    }

    /**
     * 封装mapper接口的默认resultMap到Configuration中
     *
     * @param sqlSessionFactory
     * @param genericClazz
     * @param mapperClass
     * @return
     */
    public ResultMap resultMap(SqlSessionFactory sqlSessionFactory, Class genericClazz, Class<?> mapperClass) {
        ArrayList<ResultMapping> resultMappings = new ArrayList<>();
        for (Field field : genericClazz.getDeclaredFields()) {
            // 是否忽略的字段，不加入resultMap
            if (field.isAnnotationPresent(Ignore.class)) {
                continue;
            }
            List<ResultFlag> flags = new ArrayList<>();
            if (field.isAnnotationPresent(TableId.class)) {
                flags.add(ResultFlag.ID);
            }
            // 组装resultMapping

            ;
            Class<?> clazzPropertyType = getClazzPropertyType(sqlSessionFactory, genericClazz, field.getName(), null);
            resultMappings.add(new ResultMapping.Builder(sqlSessionFactory.getConfiguration(), field.getName(), getColumnName(field), clazzPropertyType)
                    .jdbcType(null)
                    .nestedQueryId(null)
                    .nestedResultMapId(null)
                    .resultSet(null)
                    .typeHandler(null)
                    .flags(flags == null ? new ArrayList<>() : flags)
                    .composites(Collections.emptyList())
                    .notNullColumns(new HashSet<>())
                    .columnPrefix(null)
                    .foreignColumn(null)
                    .lazy(false)
                    .build());
        }
        String resultId = getResultId(genericClazz, mapperClass);
        ResultMap resultMap = new ResultMap.Builder(sqlSessionFactory.getConfiguration(), resultId, genericClazz, resultMappings, null).discriminator(null)
                .build();

        sqlSessionFactory.getConfiguration().addResultMap(resultMap);
        return resultMap;
    }

    /**
     * 获取clazz中指定字段的java类型
     *
     * @param sqlSessionFactory
     * @param clazz
     * @param property
     * @param javaType
     * @return
     */
    public Class<?> getClazzPropertyType(SqlSessionFactory sqlSessionFactory, Class<?> clazz, String property, Class<?> javaType) {
        if (javaType == null && property != null) {
            try {
                MetaClass metaResultType = MetaClass.forClass(clazz, sqlSessionFactory.getConfiguration().getReflectorFactory());
                javaType = metaResultType.getSetterType(property);
            } catch (Exception e) {
                //ignore, following null check statement will deal with the situation
            }
        }
        if (javaType == null) {
            javaType = Object.class;
        }
        return javaType;
    }

    /**
     * 获取列名称
     *
     * @param field
     * @return
     */
    public String getColumnName(Field field) {
        String idColumnName = getTableIdColumn(field);
        if (StringUtils.isNotEmpty(idColumnName)) {
            return idColumnName;
        }
        TableField tableFieldAn = field.getAnnotation(TableField.class);
        if (tableFieldAn != null) {
            return tableFieldAn.value();
        }
        return field.getName();
    }

    /**
     * 获取tableId的列名称
     *
     * @param field
     * @return
     */
    public String getTableIdColumn(Field field) {
        String columnName = "";
        TableId annotation = field.getAnnotation(TableId.class);
        if (ObjectUtils.isEmpty(annotation)) {
            return columnName;
        }
        switch (annotation.type()) {
            case AUTO:
                break;
            default:
                columnName = StringUtils.isNoneBlank(annotation.value()) ? annotation.value() : field.getName();
        }
        return columnName;
    }
    protected String getTableFields(Class aClass) {
        String fieldsStr = "(";
        for (Field field : aClass.getDeclaredFields()) {
            if (field.isAnnotationPresent(Ignore.class)) {
                continue;
            }
            fieldsStr += "#{" + field.getName() + "},";
        }
        fieldsStr = fieldsStr.substring(0, fieldsStr.lastIndexOf(",")) + ")";
        return fieldsStr;
    }
    protected String getQTableColumns(Class aClass) {
        String fieldsStr = "";
        for (Field field : aClass.getDeclaredFields()) {
            if (field.isAnnotationPresent(Ignore.class)) {
                continue;
            }
            fieldsStr += getColumnName(field) + ",";
        }
        fieldsStr = fieldsStr.substring(0, fieldsStr.lastIndexOf(","));
        return fieldsStr;
    }

    protected String getTableColumns(Class aClass) {
        String fieldsStr = "(";
        for (Field field : aClass.getDeclaredFields()) {
            if (field.isAnnotationPresent(Ignore.class)) {
                continue;
            }
            fieldsStr += getColumnName(field) + ",";
        }
        fieldsStr = fieldsStr.substring(0, fieldsStr.lastIndexOf(",")) + ")";
        return fieldsStr;
    }

    public String getTableName(Class aClass) {
        TableName tableAn = (TableName) aClass.getAnnotation(TableName.class);
        if (tableAn != null) {
            return tableAn.value();
        }
        return aClass.getSimpleName();
    }
    public abstract String getId(Class intfClass);

    public abstract SqlCommandType getSqlCommandType();

    public abstract SqlSource sqlSource(SqlSessionFactory sqlSessionFactory, Class genericClazz);

    public abstract List<ResultMap> getResultMaps(SqlSessionFactory sqlSessionFactory, Class genericClazz, Class<?> intfClass);

}
