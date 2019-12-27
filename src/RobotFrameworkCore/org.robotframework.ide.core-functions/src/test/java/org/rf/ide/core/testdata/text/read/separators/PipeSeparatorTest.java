/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.read.separators;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.rf.ide.core.testdata.text.read.separators.Separator.SeparatorType;

public class PipeSeparatorTest {

    private PipeSeparator separator;

    @Test
    public void testCaseLine_withMultipleSpaceInFirstColumn() {
        final String theFirstSeparator = "|";
        final String theSecondSeparator = "    | ";
        final String action = "${dict} =";
        final String lastSeparator = "  |   ";
        final String text = theFirstSeparator + theSecondSeparator + action + lastSeparator;
        separator = new PipeSeparator(0, text);

        assertThat(separator.hasNextSeparator()).isTrue();
        Separator sep = separator.nextSeparator();
        assertThat(sep).isNotNull();
        assertThat(sep.getLineNumber()).isEqualTo(0);
        assertThat(sep.getStartColumn()).isEqualTo(0);
        assertThat(sep.getEndColumn()).isEqualTo(theFirstSeparator.length());
        assertThat(sep.getText().toString()).isEqualTo(theFirstSeparator);
        assertThat(sep.getTypes()).containsExactly(SeparatorType.PIPE);

        assertThat(separator.hasNextSeparator()).isTrue();

        sep = separator.nextSeparator();
        assertThat(sep).isNotNull();
        assertThat(sep.getLineNumber()).isEqualTo(0);
        assertThat(sep.getStartColumn()).isEqualTo(theFirstSeparator.length());
        assertThat(sep.getEndColumn()).isEqualTo(theFirstSeparator.length() + theSecondSeparator.length());
        assertThat(sep.getText().toString()).isEqualTo(theSecondSeparator);
        assertThat(sep.getTypes()).containsExactly(SeparatorType.PIPE);

        assertThat(separator.hasNextSeparator()).isTrue();

        sep = separator.nextSeparator();
        assertThat(sep).isNotNull();
        assertThat(sep.getLineNumber()).isEqualTo(0);
        assertThat(sep.getStartColumn())
                .isEqualTo(theFirstSeparator.length() + theSecondSeparator.length() + action.length());
        assertThat(sep.getEndColumn()).isEqualTo(text.length());
        assertThat(sep.getTypes()).containsExactly(SeparatorType.PIPE);

        assertThat(separator.hasNextSeparator()).isFalse();
    }

    @Test
    public void twoSeparatorsAt_theBegin() {
        final String theFirstSeparator = "|";
        final String theSecondSeparator = " | ";
        final String text = theFirstSeparator + theSecondSeparator + "...    foobar";
        separator = new PipeSeparator(0, text);

        assertThat(separator.hasNextSeparator()).isTrue();
        Separator sep = separator.nextSeparator();
        assertThat(sep).isNotNull();
        assertThat(sep.getLineNumber()).isEqualTo(0);
        assertThat(sep.getStartColumn()).isEqualTo(0);
        assertThat(sep.getEndColumn()).isEqualTo(theFirstSeparator.length());
        assertThat(sep.getText().toString()).isEqualTo(theFirstSeparator);
        assertThat(sep.getTypes()).containsExactly(SeparatorType.PIPE);

        assertThat(separator.hasNextSeparator()).isTrue();

        sep = separator.nextSeparator();
        assertThat(sep).isNotNull();
        assertThat(sep.getLineNumber()).isEqualTo(0);
        assertThat(sep.getStartColumn()).isEqualTo(theFirstSeparator.length());
        assertThat(sep.getEndColumn()).isEqualTo(theFirstSeparator.length() + theSecondSeparator.length());
        assertThat(sep.getText().toString()).isEqualTo(theSecondSeparator);
        assertThat(sep.getTypes()).containsExactly(SeparatorType.PIPE);
        assertThat(separator.hasNextSeparator()).isFalse();
    }

    @Test
    public void singleSeparatorAt_theBegin() {
        final String theFirstSeparator = "| ";
        separator = new PipeSeparator(0, theFirstSeparator);

        assertThat(separator.hasNextSeparator()).isTrue();
        final Separator sep = separator.nextSeparator();
        assertThat(sep).isNotNull();
        assertThat(sep.getLineNumber()).isEqualTo(0);
        assertThat(sep.getStartColumn()).isEqualTo(0);
        assertThat(sep.getEndColumn()).isEqualTo(theFirstSeparator.length());
        assertThat(sep.getText().toString()).isEqualTo(theFirstSeparator);
        assertThat(sep.getTypes()).containsExactly(SeparatorType.PIPE);

        assertThat(separator.hasNextSeparator()).isFalse();
    }

    @Test
    public void no_separator() {
        separator = new PipeSeparator(0, "  test");

        assertThat(separator.hasNextSeparator()).isFalse();
    }
}
