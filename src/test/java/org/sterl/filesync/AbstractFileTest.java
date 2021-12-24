package org.sterl.filesync;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public abstract class AbstractFileTest {

    protected final TestDataGenerator testData = new TestDataGenerator();
    @BeforeEach
    protected void setUp() throws Exception {
        testData.clean();
    }

}
