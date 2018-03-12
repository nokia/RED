/*
* Copyright 2017 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.rf.ide.core.execution.agent;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class StatusTest {

    @Test
    public void ensuringParsingTest() {
        assertThat(Status.valueOf("FAIL")).isEqualTo(Status.FAIL);
        assertThat(Status.valueOf("PASS")).isEqualTo(Status.PASS);
        assertThat(Status.valueOf("RUNNING")).isEqualTo(Status.RUNNING);
    }
}
