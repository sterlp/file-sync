package org.sterl.filesync.sync.activity;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;

import org.sterl.filesync.SimpleSyncMeta;
import org.sterl.filesync.file.FileUtil;

public class RemoveOrphanBATest {
    private final SimpleSyncMeta simpleSync = new SimpleSyncMeta();
    
    @BeforeEach
    public void before() throws IOException {
        simpleSync.clean();
        Files.copy(simpleSync.source_f1, simpleSync.destination_f1);
    }
    
    @Test
    public void testRemoveOrphanFile() throws FileNotFoundException, IOException {
        FileUtil.writeToFile(simpleSync.destinationDir.resolve("ffffo.txt"), "bar");
        FileUtil.writeToFile(simpleSync.destinationDir.resolve("ffffo2.txt"), "bar2");
        
        RemoveOrphanBA removeOrphan = new RemoveOrphanBA(simpleSync.destinationDir, simpleSync.sourceDir, Collections.emptyList());
        assertEquals(Long.valueOf(2), removeOrphan.call());
        assertFalse(Files.exists(simpleSync.destinationDir.resolve("ffffo.txt")));
    }
    
    @Test
    public void testRemoveOrphanDirs() throws FileNotFoundException, IOException {
        Files.createDirectories(simpleSync.destinationDir.resolve("xx/zz"));
        Files.createDirectories(simpleSync.destinationDir.resolve("yy"));
        FileUtil.writeToFile(simpleSync.destinationDir.resolve("yy/ffffo2.txt"), "bar2");
        
        RemoveOrphanBA removeOrphan = new RemoveOrphanBA(simpleSync.destinationDir, simpleSync.sourceDir, Collections.emptyList());
        assertEquals(Long.valueOf(4), removeOrphan.call());
        assertFalse(Files.exists(simpleSync.destinationDir.resolve("xx/zz")));
    }
    
    @Test
    public void testIgnoreList() throws FileNotFoundException, IOException {
        Path fileToIgnore = simpleSync.destinationDir.resolve("fu.txt");
        FileUtil.writeToFile(fileToIgnore, "bar2");
        FileUtil.writeToFile(simpleSync.destinationDir.resolve("fu2.txt"), "bar2");
        RemoveOrphanBA removeOrphan = new RemoveOrphanBA(
                simpleSync.destinationDir, simpleSync.sourceDir, 
                Arrays.asList(fileToIgnore));
        assertEquals(Long.valueOf(1), removeOrphan.call());
        assertTrue(Files.exists(fileToIgnore));
    }
}
