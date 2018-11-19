package org.sterl.filesync.copy.actvity;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.sterl.filesync.SimpleSyncMeta;
import org.sterl.filesync.file.FileUtil;

public class SimpleCopyStrategyTest {
    SimpleSyncMeta simpleSync = new SimpleSyncMeta();
    SimpleCopyStrategy simpleCopy;
    @Before
    public void before() throws IOException {
        simpleSync.clean();
        simpleCopy = new SimpleCopyStrategy(simpleSync.sourceDir, simpleSync.destinationDir);
    }
    @Test
    public void testFileCopy() throws IOException {
        assertTrue(simpleCopy.changed(simpleSync.source_f1));
        assertFalse(simpleCopy.changed(simpleSync.source_f1));
        
        FileUtil.isSameFile(simpleSync.source_f1, simpleSync.destinationDir.resolve(simpleSync.source_f1.getFileName()));
    }
    
    @Test
    public void testDeepFileCopy() throws IOException {
        assertTrue(simpleCopy.changed(simpleSync.source_a_b_f1));
        assertFalse(simpleCopy.changed(simpleSync.source_a_b_f1));
        
        FileUtil.isSameFile(simpleSync.source_a_b_f1, 
                simpleSync.destinationDir.resolve(simpleSync.sourceDir.relativize(simpleSync.source_a_b_f1)));
    }

}
