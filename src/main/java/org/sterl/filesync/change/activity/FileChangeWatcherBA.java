package org.sterl.filesync.change.activity;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.ClosedWatchServiceException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.sterl.filesync.config.FileSyncConfig;
import org.sterl.filesync.copy.actvity.CopyStrategy;

@Component
public class FileChangeWatcherBA implements Closeable, Callable<Long> {
    private static final Logger LOG = LoggerFactory.getLogger(FileChangeWatcherBA.class);
    
    private static final WatchEvent.Kind<?>[] WATCH_KEYS = {
        StandardWatchEventKinds.ENTRY_CREATE,
        StandardWatchEventKinds.ENTRY_DELETE,
        StandardWatchEventKinds.ENTRY_MODIFY};

    private final WatchService service;
    private final CopyStrategy strategy;
    private final FileSyncConfig config;
    
    private final Map<WatchKey, Path> watchers = new HashMap<>();
    private final AtomicBoolean shouldRun = new AtomicBoolean(false);
    private final AtomicLong changeCount = new AtomicLong(0);
    
    @Autowired
    public FileChangeWatcherBA(WatchService service, 
            CopyStrategy copyStrategy, FileSyncConfig config) throws IOException {
        super();
        this.service = service;
        this.strategy = copyStrategy;
        this.config = config;
    }
    
    @Override
    public Long call() throws Exception {
        registerDirs();
        shouldRun.set(true);

        while(shouldRun.get()) {
            try {
                final WatchKey key = service.take();
                if (key != null) {
                    final Path changedPath = this.watchers.get(key);
                    if (changedPath != null) {
                        List<WatchEvent<?>> changes = key.pollEvents();
                        key.reset();
                        handleChanges(changedPath, changes);
                    } else {
                        LOG.warn("Invalid change key detected {} -- will cancel it.", key);
                        key.cancel();
                    }
                }
            } catch (InterruptedException | ClosedWatchServiceException e) {
                if (shouldRun.get()) {
                    LOG.warn("Watch services terminated abnormally. {}", e.getMessage());
                    shouldRun.set(false);
                } else {
                    Thread.interrupted();
                }
            } catch(Exception e) {
                LOG.warn("Detected error in handling files!", e);
            }
        }

        shouldRun.set(false);
        LOG.info("Stopped watching.");
        return changeCount.get();
    }
    
    private void registerDirs() throws IOException {
        synchronized (shouldRun) {
            Files.walkFileTree(config.getSourceDir(), Collections.emptySet(), config.getMaxChangeListenDepth(), 
                    new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    return regiserDir(dir) ? FileVisitResult.CONTINUE : FileVisitResult.TERMINATE;
                }
            });
        }
    }
    /**
     * Registers a watcher on the given directory and returns <code>true</code> if more are allowed.
     * @param dir directory to watch
     * @return <code>true</code> if more watchers are allowed, otherwise <code>false</code>
     * @throws IOException
     */
    private boolean regiserDir(Path dir) throws IOException {
        if (watchers.size() < config.getMaxChangeListenDirectories()) {
            final WatchKey key = dir.register(service, WATCH_KEYS);
            watchers.put(key, dir);
            LOG.debug("Watching {}", dir);
            return watchers.size() < config.getMaxChangeListenDirectories() ? true : false;
        }
        return false;
    }

    public boolean isRunning() {
        return shouldRun.get() && !watchers.isEmpty();
    }

    public long getChangeCount() {
        return changeCount.get();
    }

    private void handleChanges(Path changedPath, List<WatchEvent<?>> changes) throws IOException {
        if (changes != null && !changes.isEmpty()) {
            for (WatchEvent<?> e : changes) {
                if (config.isNotIgnored((Path)e.context())) {
                    final Path eventPath = changedPath.resolve((Path)e.context());
                    LOG.debug("{} change detected in {}", e.kind(), eventPath);
                    try {
                        if (e.kind() == StandardWatchEventKinds.ENTRY_DELETE) {
                            strategy.deleted(eventPath);
                        } else if (e.kind() == StandardWatchEventKinds.ENTRY_MODIFY) {
                            strategy.changed(eventPath);
                        } else {
                            strategy.created(eventPath);
                        }
                        changeCount.incrementAndGet();
                    } catch (Exception ex) {
                        LOG.error("Failed to handle file {}", eventPath, ex);
                    }
                } else {
                    LOG.debug("File {} is in ignore list.", e.context());
                }

            }; 
        }
    }

    @Override
    public void close() throws IOException {
        shouldRun.set(false);
        synchronized(shouldRun) {
            this.watchers.keySet().forEach(w -> {
                w.cancel();
            });
            this.watchers.clear();
            service.close();
        }
    }
}