/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.formatter;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class RightTrimFormatterTest {

    @Test
    public void linesAreRightTrimmed() throws Exception {
        final ILineFormatter formatter = new RightTrimFormatter();

        assertThat(formatter.format("text")).isEqualTo("text");
        assertThat(formatter.format("text ")).isEqualTo("text");
        assertThat(formatter.format("text  ")).isEqualTo("text");
        assertThat(formatter.format(" text")).isEqualTo(" text");
        assertThat(formatter.format("  text")).isEqualTo("  text");
        assertThat(formatter.format(" text ")).isEqualTo(" text");
        assertThat(formatter.format("  text  ")).isEqualTo("  text");
    }

}
