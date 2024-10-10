package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.entity.Orders;
import com.sky.vo.StatisticsVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface OrderMapper {
    /**
     * 插入订单数据
     * @param orders
     */
    void insert(Orders orders);

    /**
     * 根据订单号查询订单
     * @param orderNumber
     */
    @Select("select * from orders where number = #{orderNumber}")
    Orders getByNumber(String orderNumber);

    /**
     * 修改订单信息
     * @param orders
     */
    void update(Orders orders);
    /**
     * 查询当前用户订单
     * @param ordersPageQueryDTO
     * @return
     */
    Page<Orders> pageQuery(OrdersPageQueryDTO ordersPageQueryDTO);

    /**
     * 根据主键id查询订单
     * @param id
     * @return
     */
    @Select("select * from orders where id = #{id}")
    Orders getById(Long id);

    /**
     * 统计各个状态订单数量
     * @param status
     * @return
     */
    @Select("select count(id) from orders where status = #{status}")
    Integer countStatus(Integer status);

    /**
     * 根据订单状态和下单时间查询订单
     * @param status
     * @param orderTime
     * @return
     */
    //select * from orders where status=待付款 and order_time<(当前时间-15分钟)
    @Select("select * from orders where status = #{status} and order_time < #{orderTime}")
    List<Orders>getByStatusAndOrderTimeLT(Integer status, LocalDateTime orderTime);

    /**
     * 根据传入的日期列表统计每天订单金额
     * @param dateList
     * @return
     */
    List<StatisticsVO<BigDecimal>> getTurnoverStatistics(List<LocalDate> dateList);


    /**
     * 根据传入的日期列表和status统计每天订单数量当status=0就查总数
     * @param dateList
     * @return
     */
    List<StatisticsVO<Long>> getOrderStatistics(List<LocalDate> dateList,Integer status);

    /**
     * 根据传入的日期列表统计该时间段菜品销售量最高前十
     * @param begin
     * @param end
     * @return
     */
    @Select("SELECT od.name date, sum(od.number) as amount  from order_detail od,orders o WHERE " +
            "od.order_id=o.id and o.status=5 and DATE_FORMAT(o.order_time,'%Y-%m-%d') " +
            "BETWEEN #{begin} and #{end}  GROUP BY date order by amount desc LIMIT 0,10;")
    List<StatisticsVO<Integer>> getSalesStatistics(LocalDate begin,LocalDate end);
}
