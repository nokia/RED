/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.formatter;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class AdjustsConstantSeparatorsFormatterTest {

    @Test
    public void separatorLengthsAreAdjusted() throws Exception {
        final ILineFormatter formatter = new AdjustsConstantSeparatorsFormatter(2);

        assertThat(formatter.format("text")).isEqualTo("text");
        assertThat(formatter.format(" text")).isEqualTo(" text");
        assertThat(formatter.format("  text")).isEqualTo("  text");
        assertThat(formatter.format("   text")).isEqualTo("  text");
        assertThat(formatter.format("   text abc")).isEqualTo("  text abc");
        assertThat(formatter.format("   text  abc")).isEqualTo("  text  abc");
        assertThat(formatter.format("   text   abc")).isEqualTo("  text  abc");
        assertThat(formatter.format("   text   abc ")).isEqualTo("  text  abc ");
        assertThat(formatter.format("   text   abc  ")).isEqualTo("  text  abc  ");
        assertThat(formatter.format("   text   abc   ")).isEqualTo("  text  abc  ");
    }
}
