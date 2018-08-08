/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.reader.recognizer;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.rf.ide.core.testdata.text.read.recognizer.PreviousLineContinueRecognizer;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

public class PreviousLineContinueRecognizerTest {

    private final PreviousLineContinueRecognizer rec = new PreviousLineContinueRecognizer();

    @Test
    public void test_ThreeDotsAndFoobarWord() {
        final StringBuilder text = new StringBuilder("...foobar");

        assertThat(rec.hasNext(text, 1, 0)).isTrue();
        final RobotToken token = rec.next();
        assertThat(token.getStartColumn()).isEqualTo(0);
        assertThat(token.getLineNumber()).isEqualTo(1);
        assertThat(token.getEndColumn()).isEqualTo(3);
        assertThat(token.getText().toString()).isEqualTo("...");
        assertThat(token.getTypes()).containsExactly(rec.getProducedType());
    }

    @Test
    public void test_FourDots() {
        final StringBuilder text = new StringBuilder("....");

        assertThat(rec.hasNext(text, 1, 0)).isTrue();
        final RobotToken token = rec.next();
        assertThat(token.getStartColumn()).isEqualTo(0);
        assertThat(token.getLineNumber()).isEqualTo(1);
        assertThat(token.getEndColumn()).isEqualTo(text.length());
        assertThat(token.getText().toString()).isEqualTo(text.toString());
        assertThat(token.getTypes()).containsExactly(rec.getProducedType());
    }

    @Test
    public void test_ThreeDots() {
        final StringBuilder text = new StringBuilder("...");

        assertThat(rec.hasNext(text, 1, 0)).isTrue();
        final RobotToken token = rec.next();
        assertThat(token.getStartColumn()).isEqualTo(0);
        assertThat(token.getLineNumber()).isEqualTo(1);
        assertThat(token.getEndColumn()).isEqualTo(text.length());
        assertThat(token.getText().toString()).isEqualTo(text.toString());
        assertThat(token.getTypes()).containsExactly(rec.getProducedType());
    }

    @Test
    public void test_TwoDots() {
        final StringBuilder text = new StringBuilder("..");

        assertThat(rec.hasNext(text, 1, 0)).isFalse();
    }

    @Test
    public void test_singleDot() {
        final StringBuilder text = new StringBuilder(".");

        assertThat(rec.hasNext(text, 1, 0)).isFalse();
    }

    @Test
    public void test_getPattern() {
        assertThat(rec.getPattern().pattern()).isEqualTo("^[.]{3,}");
    }

    @Test
    public void test_getProducedType() {
        assertThat(rec.getProducedType()).isEqualTo(RobotTokenType.PREVIOUS_LINE_CONTINUE);
    }
}
