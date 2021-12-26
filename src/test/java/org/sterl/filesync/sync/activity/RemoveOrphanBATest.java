package org.sterl.filesync.sync.activity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;

import org.sterl.filesync.TestDataGenerator;
import org.sterl.filesync.file.FileUtil;

public class RemoveOrphanBATest {
    private final TestDataGenerator simpleSync = new TestDataGenerator();
    
    @BeforeEach
    public void before() throws IOException {
        simpleSync.reset();
        Files.copy(simpleSync.source_f1, simpleSync.destination_f1);
    }
    
    @Test
    public void testRemoveOrphanFile() throws FileNotFoundException, IOException {
        FileUtil.appendToFile(simpleSync.destinationDir.resolve("ffffo.txt"), "bar");
        FileUtil.appendToFile(simpleSync.destinationDir.resolve("ffffo2.txt"), "bar2");
        
        RemoveOrphanBA removeOrphan = new RemoveOrphanBA(simpleSync.destinationDir, simpleSync.sourceDir, Collections.emptyList());
        assertThat(removeOrphan.call()).isEqualTo(2);
        assertFalse(Files.exists(simpleSync.destinationDir.resolve("ffffo.txt")));
    }
    
    @Test
    public void testRemoveOrphanDirs() throws FileNotFoundException, IOException {
        Files.createDirectories(simpleSync.destinationDir.resolve("xx/zz"));
        Files.createDirectories(simpleSync.destinationDir.resolve("yy"));
        FileUtil.appendToFile(simpleSync.destinationDir.resolve("yy/ffffo2.txt"), "bar2");
        
        RemoveOrphanBA removeOrphan = new RemoveOrphanBA(simpleSync.destinationDir, simpleSync.sourceDir, Collections.emptyList());
        assertThat(removeOrphan.call()).isEqualTo(4);
        assertFalse(Files.exists(simpleSync.destinationDir.resolve("xx/zz")));
    }
    
    @Test
    public void testIgnoreList() throws FileNotFoundException, IOException {
        Path fileToIgnore = simpleSync.destinationDir.resolve("fu.txt");
        FileUtil.appendToFile(fileToIgnore, "bar2");
        FileUtil.appendToFile(simpleSync.destinationDir.resolve("fu2.txt"), "bar2");
        RemoveOrphanBA removeOrphan = new RemoveOrphanBA(
                simpleSync.destinationDir, simpleSync.sourceDir, 
                Arrays.asList("fu.txt"));
        assertThat(removeOrphan.call()).isOne();
        assertTrue(Files.exists(fileToIgnore));
    }
}
