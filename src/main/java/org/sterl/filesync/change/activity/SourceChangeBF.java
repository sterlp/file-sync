package org.sterl.filesync.change.activity;

import java.io.IOException;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class SourceChangeBF {
    private static final Logger LOGGER = LoggerFactory.getLogger(SourceChangeBF.class);

    private final ExecutorService es;
    @Autowired
    private FileChangeWatcherBA changeWatcher;
    
    public SourceChangeBF() {
        BasicThreadFactory build = new BasicThreadFactory.Builder()
            .daemon(true)
            .namingPattern("FileSync-ChangeListener")
            .priority(Thread.NORM_PRIORITY - 2)
            .uncaughtExceptionHandler(new UncaughtExceptionHandler() {
                @Override
                public void uncaughtException(Thread t, Throwable e) {
                    LOGGER.error("File chnage listener failed.", e);
                }
            })
            .build();
        es = Executors.newFixedThreadPool(1, build);
    }

    @PostConstruct
    public void start() {
        //es.submit(changeWatcher);
    }

    @Scheduled(initialDelay = 5_000, fixedRate = 30_000)
    void checkStatus() {
        if (changeWatcher.isRunning()) {
            LOGGER.info("Change listener is running. Change count: {}", changeWatcher.getChangeCount());
        } else {
            LOGGER.info("Change listener starting ...");
            es.submit(changeWatcher);
        }
    }

    @PreDestroy
    public void stop() {
        try {
            changeWatcher.close();
        } catch (IOException e) {
            LOGGER.info("Failed sto stop dir watcher: {}", e.getMessage());
        }
        es.shutdownNow();
    }
}