/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.reader.recognizer;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.rf.ide.core.test.helpers.ClassFieldCleaner;
import org.rf.ide.core.test.helpers.ClassFieldCleaner.ForClean;
import org.rf.ide.core.testdata.text.read.recognizer.ATokenRecognizer;
import org.rf.ide.core.testdata.text.read.recognizer.PreviousLineContinueRecognizer;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

@SuppressWarnings("PMD.MethodNamingConventions")
public class PreviousLineContinueRecognizerTest {

    @ForClean
    private ATokenRecognizer rec;

    @Test
    public void test_ThreeDotsAndFoobarWord() {
        StringBuilder text = new StringBuilder("...foobar");

        assertThat(rec.hasNext(text, 1, 0)).isTrue();
        RobotToken token = rec.next();
        assertThat(token.getStartColumn()).isEqualTo(0);
        assertThat(token.getLineNumber()).isEqualTo(1);
        assertThat(token.getEndColumn()).isEqualTo(3);
        assertThat(token.getText().toString()).isEqualTo("...");
        assertThat(token.getTypes()).containsExactly(rec.getProducedType());
    }

    @Test
    public void test_FourDots() {
        StringBuilder text = new StringBuilder("....");

        assertThat(rec.hasNext(text, 1, 0)).isTrue();
        RobotToken token = rec.next();
        assertThat(token.getStartColumn()).isEqualTo(0);
        assertThat(token.getLineNumber()).isEqualTo(1);
        assertThat(token.getEndColumn()).isEqualTo(text.length());
        assertThat(token.getText().toString()).isEqualTo(text.toString());
        assertThat(token.getTypes()).containsExactly(rec.getProducedType());
    }

    @Test
    public void test_ThreeDots() {
        StringBuilder text = new StringBuilder("...");

        assertThat(rec.hasNext(text, 1, 0)).isTrue();
        RobotToken token = rec.next();
        assertThat(token.getStartColumn()).isEqualTo(0);
        assertThat(token.getLineNumber()).isEqualTo(1);
        assertThat(token.getEndColumn()).isEqualTo(text.length());
        assertThat(token.getText().toString()).isEqualTo(text.toString());
        assertThat(token.getTypes()).containsExactly(rec.getProducedType());
    }

    @Test
    public void test_TwoDots() {
        StringBuilder text = new StringBuilder("..");

        assertThat(rec.hasNext(text, 1, 0)).isFalse();
    }

    @Test
    public void test_singleDot() {
        StringBuilder text = new StringBuilder(".");

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

    @Before
    public void setUp() {
        rec = new PreviousLineContinueRecognizer();
    }

    @After
    public void tearDown() throws Exception {
        ClassFieldCleaner.init(this);
    }
}
