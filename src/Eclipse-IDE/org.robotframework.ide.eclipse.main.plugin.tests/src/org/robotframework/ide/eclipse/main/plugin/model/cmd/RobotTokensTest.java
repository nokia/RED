/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model.cmd;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class RobotTokensTest {

    @Test
    public void tokenCreationTest() {
        assertThat(RobotTokens.create("").getRaw()).isEqualTo("");
        assertThat(RobotTokens.create("").getText()).isEqualTo("");

        assertThat(RobotTokens.create("abc").getRaw()).isEqualTo("abc");
        assertThat(RobotTokens.create("abc").getText()).isEqualTo("abc");
    }

    @Test(expected = NullPointerException.class)
    public void cannotCreateTokenWithNullContent() {
        RobotTokens.create(null);
    }

}
