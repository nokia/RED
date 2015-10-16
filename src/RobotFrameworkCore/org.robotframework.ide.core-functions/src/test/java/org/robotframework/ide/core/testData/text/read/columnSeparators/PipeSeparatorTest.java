/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.core.testData.text.read.columnSeparators;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.After;
import org.junit.Test;
import org.robotframework.ide.core.testData.text.read.columnSeparators.Separator.SeparatorType;
import org.robotframework.ide.core.testHelpers.ClassFieldCleaner;
import org.robotframework.ide.core.testHelpers.ClassFieldCleaner.ForClean;


public class PipeSeparatorTest {

    @ForClean
    private PipeSeparator separator;


    @Test
    public void testCaseLine_withMultipleSpaceInFirstColumn() {
        String theFirstSeparator = "|";
        String theSecondSeparator = "    | ";
        String action = "${dict} =";
        String lastSeparator = "  |   ";
        String text = theFirstSeparator + theSecondSeparator + action
                + lastSeparator;
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
        assertThat(sep.getEndColumn()).isEqualTo(
                theFirstSeparator.length() + theSecondSeparator.length());
        assertThat(sep.getText().toString()).isEqualTo(theSecondSeparator);
        assertThat(sep.getTypes()).containsExactly(SeparatorType.PIPE);

        assertThat(separator.hasNextSeparator()).isTrue();

        sep = separator.nextSeparator();
        assertThat(sep).isNotNull();
        assertThat(sep.getLineNumber()).isEqualTo(0);
        assertThat(sep.getStartColumn()).isEqualTo(
                theFirstSeparator.length() + theSecondSeparator.length()
                        + action.length());
        assertThat(sep.getEndColumn()).isEqualTo(text.length());
        assertThat(sep.getTypes()).containsExactly(SeparatorType.PIPE);

        assertThat(separator.hasNextSeparator()).isFalse();
    }


    @Test
    public void twoSeparatorsAt_theBegin() {
        String theFirstSeparator = "|";
        String theSecondSeparator = " | ";
        String text = theFirstSeparator + theSecondSeparator + "...    foobar";
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
        assertThat(sep.getEndColumn()).isEqualTo(
                theFirstSeparator.length() + theSecondSeparator.length());
        assertThat(sep.getText().toString()).isEqualTo(theSecondSeparator);
        assertThat(sep.getTypes()).containsExactly(SeparatorType.PIPE);
        assertThat(separator.hasNextSeparator()).isFalse();
    }


    @Test
    public void singleSeparatorAt_theBegin() {
        String theFirstSeparator = "| ";
        separator = new PipeSeparator(0, theFirstSeparator);

        assertThat(separator.hasNextSeparator()).isTrue();
        Separator sep = separator.nextSeparator();
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


    @After
    public void tearDown() throws Exception {
        ClassFieldCleaner.init(this);
    }
}
