/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.formatter;

import static com.google.common.collect.Sets.newHashSet;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

public class LineIndentFormatterTest {

    @Test
    public void linesAreRightTrimmed() throws Exception {
        final ILineFormatter formatter = new LineIndentFormatter(newHashSet(1, 5, 8), 2);

        assertThat(formatter.format(0, "text")).isEqualTo("text");
        assertThat(formatter.format(1, "text")).isEqualTo("  text");
        assertThat(formatter.format(2, "text")).isEqualTo("text");
        assertThat(formatter.format(3, "text")).isEqualTo("text");
        assertThat(formatter.format(4, "text")).isEqualTo("text");
        assertThat(formatter.format(5, "text")).isEqualTo("  text");
        assertThat(formatter.format(6, "text")).isEqualTo("text");
        assertThat(formatter.format(7, "text")).isEqualTo("text");
        assertThat(formatter.format(8, "text")).isEqualTo("  text");
        assertThat(formatter.format(9, "text")).isEqualTo("text");
    }

}
