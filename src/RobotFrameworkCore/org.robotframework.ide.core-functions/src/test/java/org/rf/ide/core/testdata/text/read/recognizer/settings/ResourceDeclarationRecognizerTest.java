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

public class ResourceDeclarationRecognizerTest {

    private final ResourceDeclarationRecognizer rec = new ResourceDeclarationRecognizer();

    @Test
    public void test_resourceColonWord_allCombinations() {
        final List<String> combinations = new CombinationGenerator().combinations("Resource:");

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
    public void test_twoSpacesAndResourceColonThenWord() {
        final StringBuilder text = new StringBuilder(" Resource:");
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
    public void test_singleSpaceAndResourceColonThenWord() {
        final StringBuilder text = new StringBuilder(" Resource:");
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
    public void test_singleResourceColonThenLetterCWord() {
        final StringBuilder text = new StringBuilder("Resource:");
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
    public void test_singleResourceColonWord() {
        final StringBuilder text = new StringBuilder("Resource:");

        assertThat(rec.hasNext(text, 1, 0)).isTrue();
        final RobotToken token = rec.next();
        assertThat(token.getStartColumn()).isEqualTo(0);
        assertThat(token.getLineNumber()).isEqualTo(1);
        assertThat(token.getEndColumn()).isEqualTo(text.length());
        assertThat(token.getText().toString()).isEqualTo(text.toString());
        assertThat(token.getTypes()).containsExactly(rec.getProducedType());
    }

    @Test
    public void test_resourceWord_allCombinations() {
        final List<String> combinations = new CombinationGenerator().combinations("Resource");

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
    public void test_twoSpacesAndResourceThenWord() {
        final StringBuilder text = new StringBuilder(" Resource");
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
    public void test_singleSpaceAndResourceThenWord() {
        final StringBuilder text = new StringBuilder(" Resource");
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
    public void test_singleResourceThenLetterCWord() {
        final StringBuilder text = new StringBuilder("Resource");
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
    public void test_singleResourceWord() {
        final StringBuilder text = new StringBuilder("Resource");

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
        assertThat(rec.getPattern().pattern()).isEqualTo("[ ]?(" + ATokenRecognizer.createUpperLowerCaseWord("Resource")
                + "[\\s]*:" + "|" + ATokenRecognizer.createUpperLowerCaseWord("Resource") + ")");

    }

    @Test
    public void test_getProducedType() {
        assertThat(rec.getProducedType()).isEqualTo(RobotTokenType.SETTING_RESOURCE_DECLARATION);
    }
}
