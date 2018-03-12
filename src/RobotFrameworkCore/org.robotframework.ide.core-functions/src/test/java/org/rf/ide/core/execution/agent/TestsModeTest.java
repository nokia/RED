/*
* Copyright 2017 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.rf.ide.core.execution.agent;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class TestsModeTest {

    @Test
    public void ensuringParsingTest() {
        assertThat(TestsMode.valueOf("RUN")).isEqualTo(TestsMode.RUN);
        assertThat(TestsMode.valueOf("DEBUG")).isEqualTo(TestsMode.DEBUG);
    }
}
