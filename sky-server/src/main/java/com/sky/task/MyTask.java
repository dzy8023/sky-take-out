package com.sky.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;

//@Component
@Slf4j
public class MyTask {
    /**
     * 定时任务 每5秒执行一次
     * 秒，分，时，日，月，周几，年（可选）
     */
    @Scheduled(cron="0/5 * * * * ?")
    public void executeTask(){
        log.info("定时任务执行了:{}",new Date());
    }
}
