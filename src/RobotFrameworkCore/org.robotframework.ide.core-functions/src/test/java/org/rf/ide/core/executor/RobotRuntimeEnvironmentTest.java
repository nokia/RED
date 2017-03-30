/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.executor;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;

import org.junit.Test;

public class RobotRuntimeEnvironmentTest {

    @Test
    public void testSimpleCall_withRuntimeEnvironment_argsFile() throws IOException {
        assertThat(RobotRuntimeEnvironment.wrapArgumentIfNeeded("")).isEmpty();

        assertThat(RobotRuntimeEnvironment.wrapArgumentIfNeeded("a")).isEqualTo("a");
        assertThat(RobotRuntimeEnvironment.wrapArgumentIfNeeded("a_b")).isEqualTo("a_b");
        assertThat(RobotRuntimeEnvironment.wrapArgumentIfNeeded("\"a\"")).isEqualTo("\"a\"");

        assertThat(RobotRuntimeEnvironment.wrapArgumentIfNeeded("a b")).isEqualTo("\"a b\"");
        assertThat(RobotRuntimeEnvironment.wrapArgumentIfNeeded("\"a b")).isEqualTo("\"\"a b\"");
        assertThat(RobotRuntimeEnvironment.wrapArgumentIfNeeded("\"a b\"")).isEqualTo("\"\"a b\"\"");
    }
}
