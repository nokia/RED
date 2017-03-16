/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.executor;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class ArgumentsConverterTest {

    @Test
    public void testParsingArguments() {
        assertThat(ArgumentsConverter.parseArguments("")).isEmpty();

        assertThat(ArgumentsConverter.parseArguments("-a 1 2")).containsExactly("-a", "1", "2");
        assertThat(ArgumentsConverter.parseArguments("-a \\1 2")).containsExactly("-a", "\\1", "2");
        assertThat(ArgumentsConverter.parseArguments("-a \\\"1 2")).containsExactly("-a", "\\\"1", "2");
        assertThat(ArgumentsConverter.parseArguments("-a \"1 2\"")).containsExactly("-a", "\"1 2\"");

        assertThat(ArgumentsConverter.parseArguments("-a 1 2 --b 4")).containsExactly("-a", "1", "2", "--b", "4");
        assertThat(ArgumentsConverter.parseArguments("-a \\1 2 --b 4")).containsExactly("-a", "\\1", "2", "--b", "4");
        assertThat(ArgumentsConverter.parseArguments("-a \\\"1 2 --b 4")).containsExactly("-a", "\\\"1", "2", "--b", "4");
        assertThat(ArgumentsConverter.parseArguments("-a \"1 2\" --b 4")).containsExactly("-a", "\"1 2\"", "--b", "4");
    }

    @Test
    public void testArgumentsJoining() {
        assertThat(ArgumentsConverter.joinMultipleArgValues(newArrayList())).isEmpty();

        assertThat(ArgumentsConverter.joinMultipleArgValues(newArrayList("-a"))).containsExactly("-a");
        assertThat(ArgumentsConverter.joinMultipleArgValues(newArrayList("-a", "1", "2"))).containsExactly("-a", "1 2");
        assertThat(ArgumentsConverter.joinMultipleArgValues(newArrayList("-a", "--b"))).containsExactly("-a", "--b");
        assertThat(ArgumentsConverter.joinMultipleArgValues(newArrayList("-a", "--b", "3", "4", "5")))
                .containsExactly("-a", "--b", "3 4 5");
        assertThat(ArgumentsConverter.joinMultipleArgValues(newArrayList("-a", "1", "2", "--b", "3", "4", "5")))
                .containsExactly("-a", "1 2", "--b", "3 4 5");
    }
}
