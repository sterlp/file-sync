package org.sterl.filesync.copy.actvity;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.*;

import java.io.IOException;

import org.sterl.filesync.TestDataGenerator;
import org.sterl.filesync.file.FileUtil;
import org.sterl.filesync.sync.activity.DirectoryAdapter;

public class SimpleCopyStrategyTest {
    TestDataGenerator simpleSync = new TestDataGenerator();
    DirectoryAdapter simpleCopy;

    @BeforeEach
    public void before() throws IOException {
        simpleSync.reset();
        simpleCopy = new DirectoryAdapter(simpleSync.sourceDir, simpleSync.destinationDir);
    }

    @Test
    public void testFileCopy() throws IOException {
        assertTrue(simpleCopy.changed(simpleSync.source_f1));
        assertFalse(simpleCopy.changed(simpleSync.source_f1));
        
        FileUtil.hasFileChanged(simpleSync.source_f1, simpleSync.destinationDir.resolve(simpleSync.source_f1.getFileName()));
    }

    @Test
    public void testDeepFileCopy() throws IOException {
        assertTrue(simpleCopy.changed(simpleSync.source_a_b_f1));
        assertFalse(simpleCopy.changed(simpleSync.source_a_b_f1));
        
        FileUtil.hasFileChanged(simpleSync.source_a_b_f1, 
                simpleSync.destinationDir.resolve(simpleSync.sourceDir.relativize(simpleSync.source_a_b_f1)));
    }

}
