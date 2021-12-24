package org.sterl.filesync.sync.manager;

import java.io.IOException;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.PreDestroy;

import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.sterl.filesync.sync.activity.FileVisitorStrategy;
import org.sterl.filesync.sync.activity.MasterSlaveFileVisitorBA;
import org.sterl.filesync.sync.model.CopyFileStatistics;
import org.sterl.filesync.time.Gauge;

import lombok.RequiredArgsConstructor;

@Service @RequiredArgsConstructor
public class DirSyncBM {
    private static final Logger LOGGER = LoggerFactory.getLogger(DirSyncBM.class);

    final BasicThreadFactory build = new BasicThreadFactory.Builder()
            .daemon(true)
            .namingPattern("FileSync-DirSync")
            .priority(Thread.MIN_PRIORITY)
            .build();

    private final ExecutorService es = Executors.newFixedThreadPool(1, build);
    private final AtomicBoolean syncRunning = new AtomicBoolean(false);
    private final Gauge gauge = new Gauge();

    private final FileVisitorStrategy visitorStratregy;
    
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
            LOGGER.info("Start full directory sync for {}.", visitorStratregy.getSourceDir());
            gauge.start();
            Files.walkFileTree(visitorStratregy.getSourceDir(), visitorStratregy);
            CopyFileStatistics stats = visitorStratregy.resetStats();
            gauge.stop();
            LOGGER.info("Sync of {} finished in {}s details: {}", 
                    visitorStratregy.getSourceDir(),
                    gauge.get(), stats);
            return stats;
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            syncRunning.set(false);
        }
    }

    public boolean stopped() {
        return !syncRunning.get();
    }
}