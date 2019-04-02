/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.formatter;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import com.google.common.collect.ImmutableList;

public class AdjustsDynamicSeparatorsFormatterTest {

    @Test
    public void separatorLengthsAreAdjusted() throws Exception {
        final ILineFormatter formatter = new AdjustsDynamicSeparatorsFormatter(2, ImmutableList.of(5, 7, 4));

        assertThat(formatter.format("abc")).isEqualTo("abc    ");
        assertThat(formatter.format(" abc")).isEqualTo(" abc   ");
        assertThat(formatter.format("  abc")).isEqualTo("       abc      ");
        assertThat(formatter.format("   abc")).isEqualTo("       abc      ");
        assertThat(formatter.format("abc    def")).isEqualTo("abc    def      ");
        assertThat(formatter.format("abc   def     ghi")).isEqualTo("abc    def      ghi   ");
    }

    @Test
    public void columnLengthsAreCounted() throws Exception {
        assertThat(AdjustsDynamicSeparatorsFormatter.countColumnLengths("")).isEmpty();
        assertThat(AdjustsDynamicSeparatorsFormatter.countColumnLengths("a  bc  def  ghi")).containsExactly(1, 2, 3, 3);
        assertThat(AdjustsDynamicSeparatorsFormatter.countColumnLengths(
                String.join("\n", "test", "  Keyword  123", "  Other Long Keyword  qwerty", "Exit Kw")))
                        .containsExactly(7, 18, 6);
    }

}
