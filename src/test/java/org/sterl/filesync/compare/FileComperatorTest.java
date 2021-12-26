package org.sterl.filesync.compare;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class FileComperatorTest {

    private FileComperator subject = new FileComperator();

    @Test
    void testAdjustTimeDiff() {
        assertThat(subject.adjustTimeDiff(-1)).isFalse();
        assertThat(subject.adjustTimeDiff(0)).isFalse();
        
        assertThat(subject.adjustTimeDiff(10)).isTrue();
        assertThat(subject.adjustTimeDiff(10)).isFalse();
        assertThat(subject.getTimeDiff()).isEqualTo(10);
        
        assertThat(subject.adjustTimeDiff(1000)).isTrue();
        assertThat(subject.adjustTimeDiff(1000)).isFalse();
        assertThat(subject.getTimeDiff()).isEqualTo(1000);

        assertThat(subject.adjustTimeDiff(1001)).isFalse();
        assertThat(subject.getTimeDiff()).isEqualTo(1000);
    }

}
