package org.sterl.filesync.sync.activity;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;

import org.junit.Before;
import org.junit.Test;
import org.sterl.filesync.SimpleSyncMeta;
import org.sterl.filesync.config.FileSyncConfig;
import org.sterl.filesync.copy.actvity.SimpleCopyStrategy;
import org.sterl.filesync.file.FileUtil;

/**
 * Testing here the simple unidirectional sync
 */
public class SimpleCopyBATest {
    
    final SimpleSyncMeta simpleSyncMeta = new SimpleSyncMeta();
    final Path sourceDir = simpleSyncMeta.sourceDir;
    final Path destinationDir = simpleSyncMeta.destinationDir;
    
    SimpleCopyStrategy copyStrategy;
    
    final File testFile = new File(sourceDir.toString() + "/foo.txt");
    final File copiedTestFile = new File(destinationDir.toString() + "/foo.txt");
    final FileSyncConfig config = new FileSyncConfig(sourceDir, destinationDir, new HashSet<>(), 5, 100);

    @Before
    public void before() throws IOException {
        simpleSyncMeta.clean();
        copyStrategy = new SimpleCopyStrategy(sourceDir, destinationDir);
        config.ignore(".DS_Store");
    }

    @Test
    public void testSyncSourceToDesctination() throws IOException {
        CopyFileVisitorBA copyFileVisitorBA = new CopyFileVisitorBA(copyStrategy, config.getIgnoreList());
        Files.walkFileTree(sourceDir, copyFileVisitorBA);
        
        assertEquals(3, copyFileVisitorBA.getStats().getCopiedFilesCount());
        assertEquals(2, copyFileVisitorBA.getStats().getDirectoriesCreatedCount());
        
        copyFileVisitorBA = new CopyFileVisitorBA(copyStrategy, config.getIgnoreList());
        Files.walkFileTree(sourceDir, copyFileVisitorBA);
        assertEquals(0, copyFileVisitorBA.getStats().getCopiedFilesCount());
        assertEquals(3, copyFileVisitorBA.getStats().getFilesFoundCount());
    }

    @Test
    public void testChangeFileCopy() throws IOException {
        FileUtil.createEmpty(destinationDir);
        final CopyFileVisitorBA visitor = new CopyFileVisitorBA(copyStrategy, config.getIgnoreList());
        Files.walkFileTree(sourceDir, visitor);
        
        testFile.deleteOnExit();
        FileUtil.writeToFile(testFile, "Test Text");
        
        visitor.resetStats();
        Files.walkFileTree(sourceDir, visitor);
        assertEquals(1, visitor.getStats().getCopiedFilesCount());
        assertTrue(copiedTestFile.exists());
        assertEquals("Test Text", new String(Files.readAllBytes(copiedTestFile.toPath())));
        
        FileUtil.writeToFile(testFile, "Some more text");

        visitor.resetStats();
        Files.walkFileTree(sourceDir, visitor);
        assertEquals(4, visitor.getStats().getFilesFoundCount());
        assertEquals("Some more text", new String(Files.readAllBytes(copiedTestFile.toPath())));
        assertEquals(1, visitor.getStats().getCopiedFilesCount());
        
        visitor.resetStats();
        Files.walkFileTree(sourceDir, visitor);
        assertEquals(0, visitor.getStats().getCopiedFilesCount());
    }
    
    @Test
    public void testDeleteOrphan() throws IOException {
        Files.createDirectories(destinationDir.resolve("fooo/baaar/zzzz"));
        FileUtil.writeToFile(destinationDir.resolve("fooo/baaar/hh.dat"), "bla");
        FileUtil.writeToFile(destinationDir.resolve("fooo/.dat1"), "foo for ignored file");
        config.ignore(".dat1");
        
        final CopyFileVisitorBA visitor = new CopyFileVisitorBA(copyStrategy, config.getIgnoreList());
        Files.walkFileTree(sourceDir, visitor);
        
        assertEquals(5, visitor.getStats().getDeletedResourcesCount());
        assertFalse(Files.exists(destinationDir.resolve("./fooo")));
    }
}
