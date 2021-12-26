package org.sterl.filesync.change.activity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.*;

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
import org.awaitility.Awaitility;
import org.sterl.filesync.AsyncTestUtil;
import org.sterl.filesync.TestDataGenerator;
import org.sterl.filesync.config.FileSyncConfig;
import org.sterl.filesync.file.FileUtil;
import org.sterl.filesync.sync.activity.DirectoryAdapter;

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
    
    private final TestDataGenerator simpleSync = new TestDataGenerator();
    private final FileSyncConfig config = new FileSyncConfig(
            this.simpleSync.sourceDir, this.simpleSync.sourceDir, new HashSet<>(), 5, 100);
    
    final Path sourceDir = this.simpleSync.sourceDir;
    final Path destinationDir = this.simpleSync.destinationDir;
    DirectoryAdapter copyStrategy;
    
    final File testFile = new File(this.sourceDir.toString() + "/foo.txt");
    final File copiedTestFile = new File(this.destinationDir.toString() + "/foo.txt");

    
    @BeforeEach
    public void before() throws IOException {
        this.simpleSync.reset();
        this.config.ignore(".DS_Store");
        this.copyStrategy = new DirectoryAdapter(this.sourceDir, this.destinationDir);
    }
    
    @Test
    public void testChangeFileListener() throws Exception {
        WatchService watchService = FileSystems.getDefault().newWatchService();
        
        final ExecutorService executorService = Executors.newFixedThreadPool(1, this.threadFactory);
        assertFalse(this.copiedTestFile.exists());
        try (FileChangeWatcherBA fileChangeWatcherBA = new FileChangeWatcherBA(watchService, this.copyStrategy, this.config)) {
            executorService.submit(fileChangeWatcherBA);
            AsyncTestUtil.waitForEquals(true, () -> fileChangeWatcherBA.isRunning());

            FileUtil.appendToFile(this.testFile, "Some message");

            AsyncTestUtil.waitFor(() -> fileChangeWatcherBA.getChangeCount(), l -> l >= 1, 20, TimeUnit.SECONDS);
            assertTrue(this.copiedTestFile.exists());
            assertTrue(FileUtil.hasFileChanged(this.testFile, this.copiedTestFile));
        } finally {
            FileUtil.delete(this.testFile);
            executorService.shutdownNow();
            watchService.close();
        }
    }

    @Test
    public void testChangeFileListenerSubDir() throws Exception {
        WatchService watchService = FileSystems.getDefault().newWatchService();
        
        final ExecutorService executorService = Executors.newFixedThreadPool(1, this.threadFactory);
        final File newFile = this.sourceDir.resolve("a/b/foo.txt").toFile();
        final File copiedNewFile = this.destinationDir.resolve("a/b/foo.txt").toFile();
        final File f1 = this.sourceDir.resolve("a/f1.txt").toFile();
        final File f2 = this.sourceDir.resolve("a/b/file.txt").toFile();

        try (FileChangeWatcherBA fileChangeWatcherBA = new FileChangeWatcherBA(watchService, this.copyStrategy, this.config)) {
            executorService.submit(fileChangeWatcherBA);
            Awaitility.await().until(() -> fileChangeWatcherBA.isRunning());
            
            Thread.sleep(2);
            FileUtil.appendToFile(newFile, "new file");
            FileUtil.appendToFile(f1, "f1");
            FileUtil.appendToFile(f2, "f2");
            
            assertThat(FileUtil.compareModifiedTime(newFile, copiedNewFile)).isGreaterThan(1);

            Awaitility.await().until(() -> fileChangeWatcherBA.getChangeCount() >= 3);

            assertTrue(copiedNewFile.exists());
            assertThat(FileUtil.compareModifiedTime(newFile, copiedNewFile)).isZero();
            assertThat(fileChangeWatcherBA.getChangeCount()).isGreaterThanOrEqualTo(3L);
        } finally {
            FileUtil.delete(newFile);
            executorService.shutdownNow();
            watchService.close();
        }
    }
}
