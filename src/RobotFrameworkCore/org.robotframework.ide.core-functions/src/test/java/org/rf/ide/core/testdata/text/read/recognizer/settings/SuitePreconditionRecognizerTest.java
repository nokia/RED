/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.read.recognizer.settings;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.Test;
import org.rf.ide.core.test.helpers.CombinationGenerator;
import org.rf.ide.core.testdata.text.read.recognizer.ATokenRecognizer;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

public class SuitePreconditionRecognizerTest {

    private final SuitePreconditionRecognizer rec = new SuitePreconditionRecognizer();

    @Test
    public void test_suitePreconditionColonWord_allCombinations() {
        final List<String> combinations = new CombinationGenerator().combinations("Suite Precondition:");
        for (final String comb : combinations) {
            assertThat(rec.hasNext(comb, 1, 0)).isTrue();
            final RobotToken token = rec.next();
            assertThat(token.getStartColumn()).isEqualTo(0);
            assertThat(token.getLineNumber()).isEqualTo(1);
            assertThat(token.getEndColumn()).isEqualTo(comb.length());
            assertThat(token.getText().toString()).isEqualTo(comb);
            assertThat(token.getTypes()).containsExactly(rec.getProducedType());
        }
    }

    @Test
    public void test_twoSpacesAndSuitePreconditionColonThenWord() {
        final StringBuilder text = new StringBuilder(" Suite Precondition:");
        final StringBuilder d = new StringBuilder(" ").append(text);
        assertThat(rec.hasNext(d, 1, 0)).isTrue();
        final RobotToken token = rec.next();
        assertThat(token.getStartColumn()).isEqualTo(1);
        assertThat(token.getLineNumber()).isEqualTo(1);
        assertThat(token.getEndColumn()).isEqualTo(d.length());
        assertThat(token.getText().toString()).isEqualTo(text.toString());
        assertThat(token.getTypes()).containsExactly(rec.getProducedType());
    }

    @Test
    public void test_singleSpaceAndSuitePreconditionColonThenWord() {
        final StringBuilder text = new StringBuilder(" Suite Precondition:");
        final StringBuilder d = new StringBuilder(text).append("C");

        assertThat(rec.hasNext(d, 1, 0)).isTrue();
        final RobotToken token = rec.next();
        assertThat(token.getStartColumn()).isEqualTo(0);
        assertThat(token.getLineNumber()).isEqualTo(1);
        assertThat(token.getEndColumn()).isEqualTo(text.length());
        assertThat(token.getText().toString()).isEqualTo(text.toString());
        assertThat(token.getTypes()).containsExactly(rec.getProducedType());
    }

    @Test
    public void test_singleSuitePreconditionColonThenLetterCWord() {
        final StringBuilder text = new StringBuilder("Suite Precondition:");
        final StringBuilder d = new StringBuilder(text).append("C");

        assertThat(rec.hasNext(d, 1, 0)).isTrue();
        final RobotToken token = rec.next();
        assertThat(token.getStartColumn()).isEqualTo(0);
        assertThat(token.getLineNumber()).isEqualTo(1);
        assertThat(token.getEndColumn()).isEqualTo(text.length());
        assertThat(token.getText().toString()).isEqualTo(text.toString());
        assertThat(token.getTypes()).containsExactly(rec.getProducedType());
    }

    @Test
    public void test_singleSuitePreconditionColonWord() {
        final StringBuilder text = new StringBuilder("Suite Precondition:");

        assertThat(rec.hasNext(text, 1, 0)).isTrue();
        final RobotToken token = rec.next();
        assertThat(token.getStartColumn()).isEqualTo(0);
        assertThat(token.getLineNumber()).isEqualTo(1);
        assertThat(token.getEndColumn()).isEqualTo(text.length());
        assertThat(token.getText().toString()).isEqualTo(text.toString());
        assertThat(token.getTypes()).containsExactly(rec.getProducedType());
    }

    @Test
    public void test_suitePreconditionWord_allCombinations() {
        final List<String> combinations = new CombinationGenerator().combinations("Suite Precondition");
        for (final String comb : combinations) {
            assertThat(rec.hasNext(comb, 1, 0)).isTrue();
            final RobotToken token = rec.next();
            assertThat(token.getStartColumn()).isEqualTo(0);
            assertThat(token.getLineNumber()).isEqualTo(1);
            assertThat(token.getEndColumn()).isEqualTo(comb.length());
            assertThat(token.getText().toString()).isEqualTo(comb.toString());
            assertThat(token.getTypes()).containsExactly(rec.getProducedType());
        }
    }

    @Test
    public void test_twoSpacesAndSuitePreconditionThenWord() {
        final StringBuilder text = new StringBuilder(" Suite Precondition");
        final StringBuilder d = new StringBuilder(" ").append(text);
        assertThat(rec.hasNext(d, 1, 0)).isTrue();
        final RobotToken token = rec.next();
        assertThat(token.getStartColumn()).isEqualTo(1);
        assertThat(token.getLineNumber()).isEqualTo(1);
        assertThat(token.getEndColumn()).isEqualTo(d.length());
        assertThat(token.getText().toString()).isEqualTo(text.toString());
        assertThat(token.getTypes()).containsExactly(rec.getProducedType());
    }

    @Test
    public void test_singleSpaceAndSuitePreconditionThenWord() {
        final StringBuilder text = new StringBuilder(" Suite Precondition");
        final StringBuilder d = new StringBuilder(text).append("C");

        assertThat(rec.hasNext(d, 1, 0)).isTrue();
        final RobotToken token = rec.next();
        assertThat(token.getStartColumn()).isEqualTo(0);
        assertThat(token.getLineNumber()).isEqualTo(1);
        assertThat(token.getEndColumn()).isEqualTo(text.length());
        assertThat(token.getText().toString()).isEqualTo(text.toString());
        assertThat(token.getTypes()).containsExactly(rec.getProducedType());
    }

    @Test
    public void test_singleSuitePreconditionThenLetterCWord() {
        final StringBuilder text = new StringBuilder("Suite Precondition");
        final StringBuilder d = new StringBuilder(text).append("C");

        assertThat(rec.hasNext(d, 1, 0)).isTrue();
        final RobotToken token = rec.next();
        assertThat(token.getStartColumn()).isEqualTo(0);
        assertThat(token.getLineNumber()).isEqualTo(1);
        assertThat(token.getEndColumn()).isEqualTo(text.length());
        assertThat(token.getText().toString()).isEqualTo(text.toString());
        assertThat(token.getTypes()).containsExactly(rec.getProducedType());
    }

    @Test
    public void test_singleSuitePreconditionWord() {
        final StringBuilder text = new StringBuilder("Suite Precondition");

        assertThat(rec.hasNext(text, 1, 0)).isTrue();
        final RobotToken token = rec.next();
        assertThat(token.getStartColumn()).isEqualTo(0);
        assertThat(token.getLineNumber()).isEqualTo(1);
        assertThat(token.getEndColumn()).isEqualTo(text.length());
        assertThat(token.getText().toString()).isEqualTo(text.toString());
        assertThat(token.getTypes()).containsExactly(rec.getProducedType());
    }

    @Test
    public void test_getPattern() {
        assertThat(rec.getPattern().pattern()).isEqualTo("[ ]?"
                + ATokenRecognizer.createUpperLowerCaseWord("Suite") + "[\\s]+"
                + ATokenRecognizer.createUpperLowerCaseWord("Precondition") + "([\\s]*:)?");

    }

    @Test
    public void test_getProducedType() {
        assertThat(rec.getProducedType()).isEqualTo(RobotTokenType.SETTING_SUITE_SETUP_DECLARATION);
    }
}
