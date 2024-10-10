package com.sky.mapper;

import com.sky.entity.User;
import com.sky.vo.StatisticsVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface UserMapper {
    /**
     * 根据openid查询用户
     * @param openid
     * @return
     */
    @Select("select * from user where openid = #{openid}")
    User getByOpenid(String openid) ;

    void insert(User user);

    /**
     * 根据id获取用户信息
     * @param userId
     * @return
     */
    @Select("select * from user where id = #{userId}")
    User getById(Long userId);

    /**
     * 根据传入的日期列表统计新增用户数量
     * @param dateList
     * @return
     */
    List<StatisticsVO<Long>> getNewUserStatistics(List<LocalDate> dateList);

    /**
     * 统计用户数量
     * @return
     */
    @Select("SELECT  count(id) FROM user where DATE_FORMAT(create_time,'%Y-%m-%d') <#{date}")
    Integer getTotalUserStatistics(LocalDate date);
}
