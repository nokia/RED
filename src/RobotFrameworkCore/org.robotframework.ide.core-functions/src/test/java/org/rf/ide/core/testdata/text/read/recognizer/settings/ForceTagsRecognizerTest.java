/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.read.recognizer.settings;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.rf.ide.core.test.helpers.CombinationGenerator;
import org.rf.ide.core.testdata.text.read.recognizer.ATokenRecognizer;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

public class ForceTagsRecognizerTest {

    private final ATokenRecognizer rec = new ForceTagsRecognizer();

    @Test
    public void test_forceTagsColonWord_allCombinations() {
        final List<String> combinations = new CombinationGenerator().combinations("Force Tags:");

        for (final String comb : combinations) {
            final StringBuilder textOfHeader = new StringBuilder(comb);

            assertThat(rec.hasNext(textOfHeader, 1, 0)).isTrue();
            final RobotToken token = rec.next();
            assertThat(token.getStartColumn()).isEqualTo(0);
            assertThat(token.getLineNumber()).isEqualTo(1);
            assertThat(token.getEndColumn()).isEqualTo(textOfHeader.length());
            assertThat(token.getText().toString()).isEqualTo(textOfHeader.toString());
            assertThat(token.getTypes()).containsExactly(rec.getProducedType());
        }
    }

    @Test
    public void test_twoSpacesAndForceTagsColonThenWord() {
        final StringBuilder text = new StringBuilder(" Force Tags:");
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
    public void test_singleSpaceAndForceTagsColonThenWord() {
        final StringBuilder text = new StringBuilder(" Force Tags:");
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
    public void test_singleForceTagsColonThenLetterCWord() {
        final StringBuilder text = new StringBuilder("Force Tags:");
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
    public void test_singleForceTagsColonWord() {
        final StringBuilder text = new StringBuilder("Force Tags:");

        assertThat(rec.hasNext(text, 1, 0)).isTrue();
        final RobotToken token = rec.next();
        assertThat(token.getStartColumn()).isEqualTo(0);
        assertThat(token.getLineNumber()).isEqualTo(1);
        assertThat(token.getEndColumn()).isEqualTo(text.length());
        assertThat(token.getText().toString()).isEqualTo(text.toString());
        assertThat(token.getTypes()).containsExactly(rec.getProducedType());
    }

    @Test
    public void test_forceTagsWord_allCombinations() {
        final List<String> combinations = new CombinationGenerator().combinations("Force Tags");

        for (final String comb : combinations) {
            final StringBuilder textOfHeader = new StringBuilder(comb);

            assertThat(rec.hasNext(textOfHeader, 1, 0)).isTrue();
            final RobotToken token = rec.next();
            assertThat(token.getStartColumn()).isEqualTo(0);
            assertThat(token.getLineNumber()).isEqualTo(1);
            assertThat(token.getEndColumn()).isEqualTo(textOfHeader.length());
            assertThat(token.getText().toString()).isEqualTo(textOfHeader.toString());
            assertThat(token.getTypes()).containsExactly(rec.getProducedType());
        }
    }

    @Test
    public void test_twoSpacesAndForceTagsThenWord() {
        final StringBuilder text = new StringBuilder(" Force Tags");
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
    public void test_singleSpaceAndForceTagsThenWord() {
        final StringBuilder text = new StringBuilder(" Force Tags");
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
    public void test_singleForceTagsThenLetterCWord() {
        final StringBuilder text = new StringBuilder("Force Tags");
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
    public void test_singleForceTagsWord() {
        final StringBuilder text = new StringBuilder("Force Tags");

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
        assertThat(rec.getPattern().pattern()).isEqualTo("[ ]?(" + ATokenRecognizer.createUpperLowerCaseWord("Force")
                + "[\\s]+" + ATokenRecognizer.createUpperLowerCaseWord("Tags") + "[\\s]*:" + "|"
                + ATokenRecognizer.createUpperLowerCaseWord("Force") + "[\\s]+"
                + ATokenRecognizer.createUpperLowerCaseWord("Tags") + ")");

    }

    @Test
    public void test_getProducedType() {
        assertThat(rec.getProducedType()).isEqualTo(RobotTokenType.SETTING_FORCE_TAGS_DECLARATION);
    }
}
