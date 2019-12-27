/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.formatter;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.google.common.collect.ImmutableList;

public class LineDynamicAdjustSeparatorsFormatterTest {

    @Test
    public void separatorLengthsAreAdjusted() throws Exception {
        final ILineFormatter formatter = new LineDynamicAdjustSeparatorsFormatter("  ", ImmutableList.of(5, 7, 4));

        assertThat(formatter.format("abc")).isEqualTo("abc");
        assertThat(formatter.format(" abc")).isEqualTo(" abc");
        assertThat(formatter.format("  abc")).isEqualTo("  abc");
        assertThat(formatter.format("   abc")).isEqualTo("  abc");
        assertThat(formatter.format("abc    def")).isEqualTo("abc    def");
        assertThat(formatter.format("abc   def     ghi")).isEqualTo("abc    def      ghi");
    }

    @Test
    public void separatorLengthsAreAdjusted_2() throws Exception {
        final ILineFormatter formatter = new LineDynamicAdjustSeparatorsFormatter("  ", ImmutableList.of(3, -1, 3));

        assertThat(formatter.format("a    some long cell    def")).isEqualTo("a    some long cell  def");
        assertThat(formatter.format("ab    some long cell    def")).isEqualTo("ab   some long cell  def");
        assertThat(formatter.format("abc    some long cell    def")).isEqualTo("abc  some long cell  def");
    }

    @Test
    public void columnLengthsAreCounted() {
        assertThat(LineDynamicAdjustSeparatorsFormatter.countColumnLengths("", -1)).isEmpty();
        assertThat(LineDynamicAdjustSeparatorsFormatter.countColumnLengths("a  bc  def  ghi", -1)).containsExactly(1, 2, 3, 3);
        assertThat(LineDynamicAdjustSeparatorsFormatter.countColumnLengths(
                String.join("\n", "test", "  Keyword  123", "  Other Long Keyword  qwerty", "Exit Kw"), -1))
                        .containsExactly(7, 18, 6);
        
        assertThat(LineDynamicAdjustSeparatorsFormatter.countColumnLengths("abc  d  ef  ghij", 2)).containsExactly(-1, 1, 2, -1);
        assertThat(LineDynamicAdjustSeparatorsFormatter.countColumnLengths("abc  d  ef  ghij", 3)).containsExactly(3, 1, 2, -1);
        assertThat(LineDynamicAdjustSeparatorsFormatter.countColumnLengths("abc  d  ef  ghij", 4)).containsExactly(3, 1, 2, 4);

        assertThat(LineDynamicAdjustSeparatorsFormatter
                .countColumnLengths(String.join("\n", "abc  d  ef  ghij", "kl  m  op  qrst"), 2)).containsExactly(2, 1, 2, -1);
    }

    @Test
    public void tooLongCellDoesNotInfluenceAligning() {
        final String line1 = "cell1    some very long cell exceeding limit    cell3";
        final String line2 = "cell4    cell5    cell6";
        final String line3 = "cell7    some short cell    cell8";
        final String content = String.join("\n", line1, line2, line3);
        final ILineFormatter formatter = LineDynamicAdjustSeparatorsFormatter.create(content, 2, 20);

        assertThat(formatter.format(line1)).isEqualTo("cell1  some very long cell exceeding limit  cell3");
        assertThat(formatter.format(line2)).isEqualTo("cell4  cell5            cell6");
        assertThat(formatter.format(line3)).isEqualTo("cell7  some short cell  cell8");
    }

    @Test
    public void emptyCellAtTheLineBeginingIsNotAligned() {
        final String line1 = "this is a cell in first column";
        final String line2 = "    there is empty cell  abc";
        final String line3 = "    d    e";
        final String content = String.join("\n", line1, line2, line3);
        final ILineFormatter formatter = LineDynamicAdjustSeparatorsFormatter.create(content, 2, 40);

        assertThat(formatter.format(line1)).isEqualTo("this is a cell in first column");
        assertThat(formatter.format(line2)).isEqualTo("  there is empty cell  abc");
        assertThat(formatter.format(line3)).isEqualTo("  d                    e");
    }

}
