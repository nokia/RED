/*
* Copyright 2017 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.rf.ide.core.execution.agent;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class PausingPointTest {

    @Test
    public void ensuringParsingTest() {
        assertThat(PausingPoint.valueOf("PRE_START_KEYWORD")).isEqualTo(PausingPoint.PRE_START_KEYWORD);
        assertThat(PausingPoint.valueOf("START_KEYWORD")).isEqualTo(PausingPoint.START_KEYWORD);
        assertThat(PausingPoint.valueOf("PRE_END_KEYWORD")).isEqualTo(PausingPoint.PRE_END_KEYWORD);
        assertThat(PausingPoint.valueOf("END_KEYWORD")).isEqualTo(PausingPoint.END_KEYWORD);
    }
}
