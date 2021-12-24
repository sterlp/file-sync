package org.sterl.filesync.sync.activity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sterl.filesync.TestDataGenerator;
import org.sterl.filesync.config.FileSyncConfig;
import org.sterl.filesync.file.FileUtil;

/**
 * Testing here the simple unidirectional sync
 */
public class MasterSlaveFileVisitorBATest {
    
    final TestDataGenerator testDataGenerator = new TestDataGenerator();
    final Path sourceDir = testDataGenerator.sourceDir;
    final Path destinationDir = testDataGenerator.destinationDir;
    
    DirectoryAdapter copyStrategy;
    MasterSlaveFileVisitorBA subject;

    @BeforeEach
    public void before() throws IOException {
        testDataGenerator.clean();
        subject = new MasterSlaveFileVisitorBA(testDataGenerator.toConfig());
    }
    
    @Test
    public void testSyncSourceOneFile() throws IOException {
        // GIVEN
        testDataGenerator.clean();
        testDataGenerator.writeSourceFile("f1", "hello world");
        // WHEN
        Files.walkFileTree(sourceDir, subject);
        // THEN
        assertEquals(1, subject.getStats().getCopiedFilesCount());
        assertThat(testDataGenerator.readDestinationFile("f1")).isEqualTo("hello world");
    }
    
    @Test
    public void testChangeSourceFile() throws Exception {
        // GIVEN
        testDataGenerator.clean();
        testDataGenerator.writeSourceFile("f1.txt", "start ...");
        Files.walkFileTree(sourceDir, subject);
        
        // WHEN
        Thread.sleep(1);
        testDataGenerator.writeSourceFile("f1.txt", "changed");
        subject.resetStats();

        // THEN
        Files.walkFileTree(sourceDir, subject);
        assertThat(subject.getStats().getCopiedFilesCount()).isOne();
        assertThat(testDataGenerator.readDestinationFile("f1.txt")).isEqualTo("changed");
    }

    @Test
    public void testSyncSourceToDesctination() throws IOException {
        // GIVEN
        testDataGenerator.reset();
        // WHEN
        Files.walkFileTree(sourceDir, subject);
        // THEN
        assertEquals(3, subject.getStats().getCopiedFilesCount());
        assertEquals(2, subject.getStats().getDirectoriesCreatedCount());
        
        // WHEN
        subject.resetStats();
        Files.walkFileTree(sourceDir, subject);
        // THEN
        assertEquals(0, subject.getStats().getCopiedFilesCount());
        assertEquals(3, subject.getStats().getFilesFoundCount());
    }

    @Test
    public void testDeleteOrphan() throws IOException {
        Files.createDirectories(destinationDir.resolve("fooo/baaar/zzzz"));
        FileUtil.appendToFile(destinationDir.resolve("fooo/baaar/hh.dat"), "bla");
        
        Files.walkFileTree(sourceDir, subject);
        
        assertEquals(4, subject.getStats().getDeletedResourcesCount());
        assertFalse(Files.exists(destinationDir.resolve("./fooo")));
    }
    
    @Test
    public void testIgnore() throws IOException {
        // GIVEN
        testDataGenerator.writeDestinationFile("fooo/.dat1", "foo for ignored file");
        FileSyncConfig config = testDataGenerator.toConfig().ignore(".dat1");
        
        final MasterSlaveFileVisitorBA visitor = new MasterSlaveFileVisitorBA(config);
        Files.walkFileTree(sourceDir, visitor);
        
        assertFalse(Files.exists(destinationDir.resolve("./fooo")));
        assertFalse(Files.exists(destinationDir.resolve("./fooo/.dat1")));
    }
}
