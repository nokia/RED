/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.executor;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class ArgumentsConverterTest {

    @Test
    public void testParsingArguments() {
        assertThat(ArgumentsConverter.parseArguments("")).isEmpty();

        assertThat(ArgumentsConverter.parseArguments("-a 1 2")).containsExactly("-a", "1", "2");
        assertThat(ArgumentsConverter.parseArguments("-a \\1 2")).containsExactly("-a", "\\1", "2");
        assertThat(ArgumentsConverter.parseArguments("-a \\\"1 2")).containsExactly("-a", "\\\"1", "2");
        assertThat(ArgumentsConverter.parseArguments("-a \"1 2\"")).containsExactly("-a", "1 2");

        assertThat(ArgumentsConverter.parseArguments("-a 1 2 --b 4")).containsExactly("-a", "1", "2", "--b", "4");
        assertThat(ArgumentsConverter.parseArguments("-a \\1 2 --b 4")).containsExactly("-a", "\\1", "2", "--b", "4");
        assertThat(ArgumentsConverter.parseArguments("-a \\\"1 2 --b 4")).containsExactly("-a", "\\\"1", "2", "--b", "4");
        assertThat(ArgumentsConverter.parseArguments("-a \"1 2\" --b 4")).containsExactly("-a", "1 2", "--b", "4");
    }

    @Test
    public void testArgumentSwitch() {
        assertThat(ArgumentsConverter.isSwitchArgument("")).isFalse();

        assertThat(ArgumentsConverter.isSwitchArgument("x")).isFalse();

        assertThat(ArgumentsConverter.isSwitchArgument("-a")).isTrue();
        assertThat(ArgumentsConverter.isSwitchArgument("--b")).isTrue();
    }
}
