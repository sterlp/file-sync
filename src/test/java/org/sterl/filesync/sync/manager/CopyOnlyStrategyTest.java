package org.sterl.filesync.sync.manager;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

import org.awaitility.Awaitility;
import org.junit.jupiter.api.*;
import org.sterl.filesync.AbstractFileTest;
import org.sterl.filesync.file.FileUtil;
import org.sterl.filesync.sync.activity.CopyOnlyFileVisitorBA;

class CopyOnlyStrategyTest extends AbstractFileTest {

    final Path sourceDir = testData.sourceDir;
    final Path destinationDir = testData.destinationDir;
    DirSyncBM dirSyncBM;

    @BeforeEach
    void before() throws Exception {
        dirSyncBM = new DirSyncBM(new CopyOnlyFileVisitorBA(testData.toConfig()));
    }

    @Test
    void testBothInSync() throws Exception {
        // GIVEN
        File s1 = testData.writeSourceFile("s1.txt", "hallo source");
        File d1 = testData.writeDestinationFile("d1.txt", "hallo destination");
        
        // WHEN
        assertThat(dirSyncBM.startSync()).isTrue();
        Awaitility.await().until(() -> dirSyncBM.stopped());

        // THEN
        assertThat(s1.exists()).isTrue();
        assertThat(d1.exists()).isTrue();
        assertThat(Files.exists(sourceDir.resolve("d1.txt"))).isTrue();
        assertThat(Files.exists(destinationDir.resolve("s1.txt"))).isTrue();
    }

    @Test
    void testDestinationUpdatesSource() throws Exception {
        // GIVEN
        testData.writeSourceFile("s1.txt", "hallo source 1");
        testData.writeSourceFile("s2.txt", "hallo source 2");
        
        // WHEN
        Thread.sleep(1);
        var sd1 = testData.writeDestinationFile("s1.txt", "hello destination 1");
        assertThat(dirSyncBM.startSync()).isTrue();
        Awaitility.await().until(() -> dirSyncBM.stopped());
        
        // THEN
        assertThat(Files.readString(sd1.toPath())).isEqualTo("hello destination 1");
        assertThat(testData.readSourceFile("s1.txt")).isEqualTo("hello destination 1");
        assertThat(testData.readDestinationFile("s2.txt")).isEqualTo("hallo source 2");
    }

}
