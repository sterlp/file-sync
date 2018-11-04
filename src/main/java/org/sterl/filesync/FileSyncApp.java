package org.sterl.filesync;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.WatchService;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.sterl.filesync.config.FileSyncConfig;
import org.sterl.filesync.copy.actvity.CopyStrategy;
import org.sterl.filesync.copy.actvity.SimpleCopyBA;

@SpringBootApplication(scanBasePackages = "org.sterl.filesync")
@EnableScheduling
public class FileSyncApp {
    public static void main(String[] args) {
        SpringApplication.run(FileSyncApp.class, args);
    }
    @Autowired
    private FileSyncConfig config;

    @Bean
    public CopyStrategy newCopyStrategy() throws IOException {
        return new SimpleCopyBA(config.getSourceDir(), config.getDestinationDir());
    }
    @Bean(destroyMethod = "close")
    public WatchService newWatchService() throws IOException {
        return FileSystems.getDefault().newWatchService();
    }
    @Bean
    public Set<String> ignoreList() {
        return config.getIgnoreList();
    }
    @Bean(destroyMethod="shutdown")
    public Executor taskScheduler() {
        return Executors.newScheduledThreadPool(10,
                new BasicThreadFactory.Builder()
                    .daemon(true)
                    .namingPattern("FileSync-Scheduler")
                    .build());
    }
}