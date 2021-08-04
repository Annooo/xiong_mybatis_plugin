package com.shong.xiong_mybatis_plugin.base;

import com.shong.xiong_mybatis_plugin.condition.SqlCondition;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @auther 10349 XIONGSY
 * @create 2021/8/4
 */
public interface BaseDao<T> {
    int dynamicInsert(T t);
    int dynamicUpdate(T t);
    List<T> dynamicSelect(T t);
    int dynamicDelete(T t);
    List<T> selectByCondition(@Param("sc") SqlCondition sqlCondition);
}
