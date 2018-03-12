/*
* Copyright 2017 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.rf.ide.core.execution.agent;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class LogLevelTest {

    @Test
    public void ensuringParsingTest() {
        assertThat(LogLevel.valueOf("DEBUG")).isEqualTo(LogLevel.DEBUG);
        assertThat(LogLevel.valueOf("ERROR")).isEqualTo(LogLevel.ERROR);
        assertThat(LogLevel.valueOf("FAIL")).isEqualTo(LogLevel.FAIL);
        assertThat(LogLevel.valueOf("INFO")).isEqualTo(LogLevel.INFO);
        assertThat(LogLevel.valueOf("NONE")).isEqualTo(LogLevel.NONE);
        assertThat(LogLevel.valueOf("TRACE")).isEqualTo(LogLevel.TRACE);
        assertThat(LogLevel.valueOf("WARN")).isEqualTo(LogLevel.WARN);
    }
}
