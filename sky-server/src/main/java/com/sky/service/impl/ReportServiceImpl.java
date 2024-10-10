package com.sky.service.impl;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.service.WorkspaceService;
import com.sky.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ReportServiceImpl implements ReportService {
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private WorkspaceService workspaceService;

    /**
     * 统计指定时间区间内的营业额数据
     *
     * @param begin
     * @param end
     * @return
     */
    @Override
    public TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end) {

        List<LocalDate> dateList = getDateList(begin, end);

        List<StatisticsVO<BigDecimal>> turnoverList = orderMapper.getTurnoverStatistics(dateList);
        addNull(turnoverList, dateList, new BigDecimal("0.0"));
        turnoverList.sort(Comparator.comparing(StatisticsVO::getDate));
        //查询date日期对应的营业额数据，营业额指状态为"已完成"的订单总金额
            /*SELECT DATE_FORMAT(order_time,"%Y-%m-%d") AS order_date, SUM(amount)
            FROM orders where DATE_FORMAT(order_time,"%Y-%m-%d") in ('2024-04-20','2024-04-19')
            and status = '已完成'
            GROUP BY DATE_FORMAT(order_time,"%Y-%m-%d")
            ORDER BY order_date DESC;*/
        return TurnoverReportVO.builder()
                .dateList(StringUtils.join(dateList, ","))
                .turnoverList(StringUtils.join(turnoverList.stream().map(StatisticsVO::getAmount).collect(Collectors.toList()), ","))
                .build();
    }

    //获取日期列表
    private List<LocalDate> getDateList(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList = new ArrayList<>();
        while (!begin.isAfter(end)) {
            dateList.add(begin);
            begin = begin.plusDays(1);
        }
        return dateList;
    }

    private <T> void addNull(List<StatisticsVO<T>> statisticsList, List<LocalDate> dateList, T amount) {
        for (LocalDate localDate : dateList) {
            if (statisticsList.stream().noneMatch(statisticsVO -> statisticsVO.getDate().equals(localDate.toString()))) {
                statisticsList.add(StatisticsVO.<T>builder()
                        .date(localDate.toString())
                        .amount(amount)
                        .build());
            }
        }
    }

    /**
     * 用户数据统计
     * @param begin
     * @param end
     * @return
     */
    @Override
    public UserReportVO getUserStatistics(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList = getDateList(begin, end);
        //查询date日期对应的新增用户数据，及create_time等于date
            /*SELECT DATE_FORMAT(create_time,"%Y-%m-%d") AS date, SUM(amount)
            FROM user where DATE_FORMAT(create_time,"%Y-%m-%d") in ('2024-04-20','2024-04-19')
            and status = '已完成'
            GROUP BY DATE_FORMAT(create_time,"%Y-%m-%d")
            ORDER BY date DESC;*/
        List<StatisticsVO<Long>> newUserList = userMapper.getNewUserStatistics(dateList);
        addNull(newUserList, dateList, 0L);
        //每天的总用户量，只要create_time小于date就行
        List<StatisticsVO<Integer>> totalUserList = new ArrayList<>();
        //SELECT  count(id) FROM user where DATE_FORMAT(order_time,"%Y-%m-%d") <date;
        for (LocalDate date : dateList) {
            Integer amount = userMapper.getTotalUserStatistics(date);
            if (amount == null) {
                amount = 0;
            }
            totalUserList.add(StatisticsVO.<Integer>builder()
                    .date(date.toString())
                    .amount(amount)
                    .build());
        }
        //按date排序
        newUserList.sort(Comparator.comparing(StatisticsVO::getDate));
        totalUserList.sort(Comparator.comparing(StatisticsVO::getDate));
        return UserReportVO.builder()
                .dateList(StringUtils.join(dateList, ","))
                .newUserList(StringUtils.join(newUserList.stream().map(StatisticsVO::getAmount).collect(Collectors.toList()), ","))
                .totalUserList(StringUtils.join(totalUserList.stream().map(StatisticsVO::getAmount).collect(Collectors.toList()), ","))
                .build();
    }

    @Override
    public OrderReportVO getOrederStatistics(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList = getDateList(begin, end);
        //查询date日期对应的订单数据，及order_time等于date
            /*SELECT DATE_FORMAT(order_time,"%Y-%m-%d") AS date, count(id)
            FROM orders where DATE_FORMAT(order_time,"%Y-%m-%d") in ('2024-04-20','2024-04-19')
            and status = ?
            GROUP BY DATE_FORMAT date
            ORDER BY date DESC;*/
        /*
        在 MyBatis 中，当执行聚合函数（如 COUNT）时，即使在数据库中列的数据类型为整数（如 INT），
        MyBatis 仍然将结果作为 Long 类型返回。这是因为在 Java 中，Long 类型能够容纳更大范围的整数值，
        比 int 类型更适合表示数据库中的计数结果，以避免溢出。
         */
        List<StatisticsVO<Long>> orederCountList = orderMapper.getOrderStatistics(dateList,0);
        addNull(orederCountList, dateList, 0L);
        List<StatisticsVO<Long>> validOrderList = orderMapper.getOrderStatistics(dateList, Orders.COMPLETED);
        addNull(validOrderList, dateList, 0L);

        //按date排序
        orederCountList.sort(Comparator.comparing(StatisticsVO::getDate));
        validOrderList.sort(Comparator.comparing(StatisticsVO::getDate));

       List<Long>orderList=orederCountList.stream().map(StatisticsVO::getAmount).collect(Collectors.toList());
       List<Long>vOrderList=validOrderList.stream().map(StatisticsVO::getAmount).collect(Collectors.toList());

       long validOrderCount=vOrderList.stream().mapToLong(Long::longValue).sum();
       long totalOrderCount=orderList.stream().reduce(Long::sum).get();

       double orderCompletionRate=0.0;
        if(totalOrderCount!=0){
            orderCompletionRate=validOrderCount*1.0/totalOrderCount;
       }

        return OrderReportVO.builder()
                .dateList(StringUtils.join(dateList, ","))
                .orderCountList(StringUtils.join(orderList, ","))
                .validOrderCountList(StringUtils.join(vOrderList, ","))
                .totalOrderCount((int)totalOrderCount)
                .validOrderCount((int)validOrderCount)
                .orderCompletionRate(orderCompletionRate)
                .build();
    }
    @Override
    public SalesTop10ReportVO getSalesStatistics(LocalDate begin, LocalDate end) {
        List<StatisticsVO<Integer>> salesNumberList = orderMapper.getSalesStatistics(begin,end);
        return SalesTop10ReportVO.builder()
                .nameList(StringUtils.join(salesNumberList.stream().map(StatisticsVO::getDate).collect(Collectors.toList()),","))
                .numberList(StringUtils.join(salesNumberList.stream().map(StatisticsVO::getAmount).collect(Collectors.toList()),","))
                .build();
    }

    /**
     * 导出运营数据报表
     * @param response
     */
    @Override
    public void exportBusinessData(HttpServletResponse response) {
        //查询数据库，获取营业数据---查询近30天营业数据
        LocalDate begin = LocalDate.now().minusDays(30);
        LocalDate end = LocalDate.now().minusDays(1);
        BusinessDataVO businessData = workspaceService.getBusinessData(begin, end);

        //通过POI将数据写入Excel文件中
        InputStream in = this.getClass().getClassLoader().getResourceAsStream("template/运营数据报表模板.xlsx");
        try {
            XSSFWorkbook excel = new XSSFWorkbook(in);

            //填充数据
            XSSFSheet sheet=excel.getSheet("Sheet1");
            sheet.getRow(1).getCell(1).setCellValue("时间"+begin+"至"+end);

            XSSFRow row = sheet.getRow(3);
            row.getCell(2).setCellValue(businessData.getTurnover());
            row.getCell(4).setCellValue(businessData.getOrderCompletionRate());
            row.getCell(6).setCellValue(businessData.getNewUsers());

            row= sheet.getRow(4);
            row.getCell(2).setCellValue(businessData.getValidOrderCount());
            row.getCell(4).setCellValue(businessData.getUnitPrice());

            //填充明细数据
            List<LocalDate> dateList = getDateList(begin, end);

            //营业额
            List<StatisticsVO<BigDecimal>> turnoverList = orderMapper.getTurnoverStatistics(dateList);
            addNull(turnoverList, dateList, new BigDecimal("0.0"));
            turnoverList.sort(Comparator.comparing(StatisticsVO::getDate));

            //有效订单
            List<StatisticsVO<Long>> validOrderList = orderMapper.getOrderStatistics(dateList, Orders.COMPLETED);
            addNull(validOrderList, dateList, 0L);
            validOrderList.sort(Comparator.comparing(StatisticsVO::getDate));

            //总订单
            List<StatisticsVO<Long>> orederCountList = orderMapper.getOrderStatistics(dateList,0);
            addNull(orederCountList, dateList, 0L);
            orederCountList.sort(Comparator.comparing(StatisticsVO::getDate));
            //订单完成率 validOrder/orederCount

            //平均客单价 turnover/validOrder

            //新增用户
            List<StatisticsVO<Long>> newUserList = userMapper.getNewUserStatistics(dateList);
            addNull(newUserList, dateList, 0L);
            newUserList.sort(Comparator.comparing(StatisticsVO::getDate));
            int i;
            for (LocalDate date : dateList) {
                double temp;
                i=dateList.indexOf(date);
                row=sheet.getRow(7+i);
                row.getCell(1).setCellValue(date.toString());
                row.getCell(2).setCellValue(turnoverList.get(i).getAmount().doubleValue());
                row.getCell(3).setCellValue(validOrderList.get(i).getAmount());
                temp=orederCountList.get(i).getAmount().intValue()==0 ? 0.0:(double)validOrderList.get(i).getAmount().intValue() /orederCountList.get(i).getAmount().intValue();
                row.getCell(4).setCellValue(temp);
                temp=validOrderList.get(i).getAmount().intValue()==0 ? 0.0:turnoverList.get(i).getAmount().doubleValue() /validOrderList.get(i).getAmount().intValue();
                row.getCell(5).setCellValue(temp);
                row.getCell(6).setCellValue(newUserList.get(i).getAmount().toString());
            }

            //通过输出流将Excel文件下载到客户端浏览器
            ServletOutputStream out = response.getOutputStream();
            excel.write(out);

            out.close();
            excel.close();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}





