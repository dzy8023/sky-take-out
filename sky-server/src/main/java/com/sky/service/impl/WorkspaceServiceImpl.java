package com.sky.service.impl;

import com.sky.constant.StatusConstant;
import com.sky.mapper.DishMapper;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.WorkspaceService;
import com.sky.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class WorkspaceServiceImpl implements WorkspaceService {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private SetmealMapper setmealMapper;

    /**
     * 根据时间段统计营业数据
     *
     * @return
     */
    public BusinessDataVO getBusinessData(LocalDate begin,LocalDate end) {
        /**
         * 营业额：当日已完成订单的总金额
         * 有效订单：当日已完成订单的数量
         * 订单完成率：有效订单数 / 总订单数
         * 平均客单价：营业额 / 有效订单数
         * 新增用户：当日新增用户的数量
         */
        List<LocalDate> dateList = new ArrayList<>();
        while (!begin.isAfter(end)) {
            dateList.add(begin);
            begin = begin.plusDays(1);
        }
        //查询总订单数
        List<StatisticsVO<Long>> totalOrder = orderMapper.getOrderStatistics(dateList, 0);
        int totalOrderCount = totalOrder.isEmpty() ? 0 : totalOrder.get(0).getAmount().intValue();

        //营业额
        List<StatisticsVO<BigDecimal>> turnoverOrder = orderMapper.getTurnoverStatistics(dateList);
        double turnover = turnoverOrder.isEmpty() ? 0.0 : turnoverOrder.get(0).getAmount().doubleValue();

        //有效订单数
        List<StatisticsVO<Long>> validOrder = orderMapper.getOrderStatistics(dateList, 5);
        int validOrderCount = validOrder.isEmpty() ? 0 : validOrder.get(0).getAmount().intValue();

        double unitPrice = 0.0;

        double orderCompletionRate = 0.0;
        if (totalOrderCount != 0 && validOrderCount != 0) {
            //订单完成率
            orderCompletionRate = (double) validOrderCount / totalOrderCount;
            //平均客单价
            unitPrice = turnover / validOrderCount;
        }

        //新增用户数
        List<StatisticsVO<Long>> newUser = userMapper.getNewUserStatistics(dateList);
        Integer newUsers = newUser.isEmpty() ? 0 : newUser.get(0).getAmount().intValue();

        return BusinessDataVO.builder()
                .turnover(turnover)
                .validOrderCount(validOrderCount)
                .orderCompletionRate(orderCompletionRate)
                .unitPrice(unitPrice)
                .newUsers(newUsers)
                .build();
    }

    /**
     * 查询订单管理数据
     *
     * @return
     */
    public OrderOverViewVO getOrderOverView() {
        List<LocalDate> cur = new ArrayList<>();
        cur.add(LocalDate.now());

        //待接单
        List<StatisticsVO<Long>> orders = orderMapper.getOrderStatistics(cur, 2);
        Integer waitingOrders = orders.isEmpty() ? 0 : orders.get(0).getAmount().intValue();

        //待派送
        orders = orderMapper.getOrderStatistics(cur, 3);
        Integer deliveredOrders = orders.isEmpty() ? 0 : orders.get(0).getAmount().intValue();

        //已完成
        orders = orderMapper.getOrderStatistics(cur, 5);
        Integer completedOrders = orders.isEmpty() ? 0 : orders.get(0).getAmount().intValue();
        //已取消
        orders = orderMapper.getOrderStatistics(cur, 6);
        Integer cancelledOrders = orders.isEmpty() ? 0 : orders.get(0).getAmount().intValue();

        //全部订单
        orders = orderMapper.getOrderStatistics(cur, 0);
        Integer allOrders = orders.isEmpty() ? 0 : orders.get(0).getAmount().intValue();

        return OrderOverViewVO.builder()
                .waitingOrders(waitingOrders)
                .deliveredOrders(deliveredOrders)
                .completedOrders(completedOrders)
                .cancelledOrders(cancelledOrders)
                .allOrders(allOrders)
                .build();
    }

    /**
     * 查询菜品总览
     *
     * @return
     */
    public DishOverViewVO getDishOverView() {
        Map map = new HashMap();
        map.put("status", StatusConstant.ENABLE);
        Integer sold = dishMapper.countByMap(map);

        map.put("status", StatusConstant.DISABLE);
        Integer discontinued = dishMapper.countByMap(map);

        return DishOverViewVO.builder()
                .sold(sold)
                .discontinued(discontinued)
                .build();
    }

    /**
     * 查询套餐总览
     *
     * @return
     */
    public SetmealOverViewVO getSetmealOverView() {
        Map map = new HashMap();
        map.put("status", StatusConstant.ENABLE);
        Integer sold = setmealMapper.countByMap(map);

        map.put("status", StatusConstant.DISABLE);
        Integer discontinued = setmealMapper.countByMap(map);

        return SetmealOverViewVO.builder()
                .sold(sold)
                .discontinued(discontinued)
                .build();
    }
}
