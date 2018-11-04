package org.sterl.filesync.sync.activity;

import java.io.IOException;
import java.nio.file.Files;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.sterl.filesync.sync.model.CopyFileStatistics;

@Service
public class DirSyncBF {
    private static final Logger LOGGER = LoggerFactory.getLogger(DirSyncBF.class);

    private ExecutorService es;
    @Autowired
    private CopyFileVisitorBA copyFileVisitorBA;
    
    private final AtomicBoolean syncRunning = new AtomicBoolean(false);
    
    @PostConstruct
    void start() {
        BasicThreadFactory build = new BasicThreadFactory.Builder()
                .daemon(true)
                .namingPattern("FileSync-DirSync")
                .priority(Thread.MIN_PRIORITY)
                .build();
        es = Executors.newFixedThreadPool(1, build);
    }
    @PreDestroy
    void stop() {
        es.shutdownNow();
    }

    @Scheduled(initialDelay = 10_000, fixedRate = 60 * 60 * 1000)
    public boolean startSync() {
        if (syncRunning.compareAndSet(false, true)) {
            es.submit(() -> syncDir());
            return true;
        }
        return false;
    }

    CopyFileStatistics syncDir() {
        try {
            syncRunning.set(true);
            LOGGER.info("Start full directory sync for {}.", copyFileVisitorBA.getSourceDir());
            long time = System.currentTimeMillis();
            Files.walkFileTree(copyFileVisitorBA.getSourceDir(), copyFileVisitorBA);
            CopyFileStatistics stats = copyFileVisitorBA.resetStats();
            time = System.currentTimeMillis() - time;
            LOGGER.info("Sync of {} finished in {}s details: {}", 
                    copyFileVisitorBA.getSourceDir(),
                    (time / 1000), stats);
            return stats;
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            syncRunning.set(false);
        }
    }
}