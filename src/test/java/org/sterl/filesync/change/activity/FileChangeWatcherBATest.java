package org.sterl.filesync.change.activity;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.lang.Thread.UncaughtExceptionHandler;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.WatchService;
import java.util.HashSet;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.junit.Before;
import org.junit.Test;
import org.sterl.filesync.AsyncTestUtil;
import org.sterl.filesync.SimpleSyncMeta;
import org.sterl.filesync.config.FileSyncConfig;
import org.sterl.filesync.copy.actvity.SimpleCopyBA;
import org.sterl.filesync.file.FileUtil;

/**
 * Testing here the simple unidirectional sync
 */
public class FileChangeWatcherBATest {
    
    final ThreadFactory threadFactory = new BasicThreadFactory.Builder()
        .daemon(true)
        .uncaughtExceptionHandler(new UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                System.err.println("Error detected in async thread! " + t);
                e.printStackTrace();
            }
        })
        .build();
    
    private final SimpleSyncMeta simpleSync = new SimpleSyncMeta();
    private final FileSyncConfig config = new FileSyncConfig(
            simpleSync.sourceDir, simpleSync.sourceDir, new HashSet<>(), 5, 100);
    
    final Path sourceDir = simpleSync.sourceDir;
    final Path destinationDir = simpleSync.destinationDir;
    SimpleCopyBA copyStrategy;
    
    final File testFile = new File(sourceDir.toString() + "/foo.txt");
    final File copiedTestFile = new File(destinationDir.toString() + "/foo.txt");

    
    @Before
    public void before() throws IOException {
        simpleSync.clean();
        config.ignore(".DS_Store");
        copyStrategy = new SimpleCopyBA(sourceDir, destinationDir);
    }
    
    @Test
    public void testChangeFileListener() throws Exception {
        WatchService watchService = FileSystems.getDefault().newWatchService();
        
        final ExecutorService executorService = Executors.newFixedThreadPool(1, threadFactory);
        assertFalse(copiedTestFile.exists());
        try (FileChangeWatcherBA fileChangeWatcherBA = new FileChangeWatcherBA(watchService, copyStrategy, config)) {
            executorService.submit(fileChangeWatcherBA);
            AsyncTestUtil.waitForEquals(true, () -> fileChangeWatcherBA.isRunning());

            FileUtil.writeToFile(testFile, "Some message");

            AsyncTestUtil.waitForEquals(1L, () -> fileChangeWatcherBA.getChangeCount(), 20, TimeUnit.SECONDS);
            assertTrue(copiedTestFile.exists());
            assertTrue(FileUtil.isSameFile(testFile, copiedTestFile));
        } finally {
            FileUtil.delete(testFile);
            executorService.shutdownNow();
        }
    }

    @Test
    public void testChangeFileListenerSubDir() throws Exception {
        WatchService watchService = FileSystems.getDefault().newWatchService();
        
        final ExecutorService executorService = Executors.newFixedThreadPool(1, threadFactory);
        final File newFile = sourceDir.resolve("a/b/foo.txt").toFile();
        final File copiedNewFile = destinationDir.resolve("a/b/foo.txt").toFile();
        final File f1 = sourceDir.resolve("a/f1.txt").toFile();
        final File f2 = sourceDir.resolve("a/b/file.txt").toFile();
        try (FileChangeWatcherBA fileChangeWatcherBA = new FileChangeWatcherBA(watchService, copyStrategy, config)) {
            executorService.submit(fileChangeWatcherBA);
            AsyncTestUtil.waitForEquals(true, () -> fileChangeWatcherBA.isRunning());
            
            FileUtil.writeToFile(newFile, UUID.randomUUID().toString());
            FileUtil.writeToFile(f1, UUID.randomUUID().toString());
            FileUtil.writeToFile(f2, UUID.randomUUID().toString());

            AsyncTestUtil.waitFor(() -> fileChangeWatcherBA.getChangeCount(),
                    value -> value >= 3, 35, TimeUnit.SECONDS);
            assertTrue(copiedNewFile.exists());
            FileUtil.isSameFile(newFile, copiedNewFile);
        } finally {
            FileUtil.delete(newFile);
            executorService.shutdownNow();
        }
    }
}
