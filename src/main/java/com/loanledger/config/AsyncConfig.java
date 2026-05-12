package com.loanledger.config;
import org.springframework.context.annotation.
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
@Configuration
@EnableAsync
public class AsyncConfig{
    @Bean("riscExec")
    public ThreadPoolExecutor riscExec(){
    ExecutorService riscExec = new ThreadPoolExecutor(10, 25, 60L, TimeUnit.SECONDS, new ArrayBlockingQueue(50), new ThreadPoolExecutor.CallerRunsPolicy());
}
}