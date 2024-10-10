package com.sky.controller.admin;

import com.sky.result.Result;
import com.sky.service.ReportService;
import com.sky.vo.OrderReportVO;
import com.sky.vo.SalesTop10ReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.time.LocalDate;

/*
数据统计相关
 */
@RestController
@RequestMapping("/admin/report")
@Api(tags = "数据统计相关接口")
@Slf4j
public class ReportController {
    @Autowired
   private ReportService reportService;
    /**
     * 营业额统计
     * @param begin
     * @param end
     * @return
     */
    @GetMapping("/turnoverStatistics")
    @ApiOperation("营业额统计")
    public Result<TurnoverReportVO>turnoverStatistics(
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin ,
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end){
        log.info("营业额数据统计，开始时间：{}，结束时间：{}",begin,end);
        return Result.success(reportService.getTurnoverStatistics(begin,end));
    }
    /**
     * 用户统计
     * @param begin
     * @param end
     * @return
     */
    @GetMapping("/userStatistics")
    @ApiOperation("用户统计")
    public Result<UserReportVO>userStatistics(
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin ,
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end){
        log.info("用户数据统计，开始时间：{}，结束时间：{}",begin,end);
        return Result.success(reportService.getUserStatistics(begin,end));
    }

    /**
     * 订单统计
     * @param begin
     * @param end
     * @return
     */
    @GetMapping("/ordersStatistics")
@ApiOperation("订单统计")
    public Result<OrderReportVO>orderStatistics(
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin ,
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end){
        log.info("订单数据统计，开始时间：{}，结束时间：{}",begin,end);
        return Result.success(reportService.getOrederStatistics(begin,end));
    }
    /**
     * 销量统计
     * @param begin
     * @param end
     * @return
     */
    @GetMapping("/top10")
    @ApiOperation("销量统计")
    public Result<SalesTop10ReportVO>salesStatistics(
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin ,
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end){
        log.info("销量数据统计，开始时间：{}，结束时间：{}",begin,end);
        return Result.success(reportService.getSalesStatistics(begin,end));
    }

    @GetMapping("/export")
    @ApiOperation("导出运营数据报表")
    public void export(HttpServletResponse response){
        reportService.exportBusinessData(response);
    }



}