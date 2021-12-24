package org.sterl.filesync.sync.activity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.sterl.filesync.AbstractFileTest;

import lombok.Getter;

class AbstractDirectorySyncTest extends AbstractFileTest {

    static class Foo extends AbstractDirectorySync {
        Foo(Path sourceDir, List<String> ignoreList) {
            super(sourceDir, ignoreList);
        }
        @Getter
        private List<Path> files = new ArrayList<>();
        @Override
        protected int handle(Path path) throws IOException {
            files.add(path);
            return 1;
        }
    }

    @Test
    void testIgnoreFile() throws Exception {
        // GIVEN
        testData.writeSourceFile("s1", "s1");
        testData.writeSourceFile("s2", "s2");
        testData.writeSourceFile(".ignore", "foo");
        
        // WHEN
        Foo f = new Foo(testData.sourceDir, Arrays.asList(".ignore"));
        int v = f.call().intValue();
        
        // THEN
        assertThat(v).isEqualTo(2);
        for (var file : f.getFiles()) {
            assertThat(file.getFileName()).isNotEqualTo(".ignore");
        }
    }
}
