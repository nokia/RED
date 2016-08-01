/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.read.recognizer.settings;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.rf.ide.core.test.helpers.ClassFieldCleaner;
import org.rf.ide.core.test.helpers.ClassFieldCleaner.ForClean;
import org.rf.ide.core.test.helpers.CombinationGenerator;
import org.rf.ide.core.testdata.text.read.recognizer.ATokenRecognizer;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;

@SuppressWarnings({ "PMD.MethodNamingConventions", "PMD.TooManyMethods" })
public class VariablesDeclarationRecognizerTest {

    @ForClean
    private ATokenRecognizer rec;

    @Test
    public void test_variablesColonWord_allCombinations() {
        List<String> combinations = new CombinationGenerator().combinations("Variables:");

        for (String comb : combinations) {
            StringBuilder textOfHeader = new StringBuilder(comb);

            assertThat(rec.hasNext(textOfHeader, 1, 0)).isTrue();
            RobotToken token = rec.next();
            assertThat(token.getStartColumn()).isEqualTo(0);
            assertThat(token.getLineNumber()).isEqualTo(1);
            assertThat(token.getEndColumn()).isEqualTo(textOfHeader.length());
            assertThat(token.getText().toString()).isEqualTo(textOfHeader.toString());
            assertThat(token.getTypes()).containsExactly(rec.getProducedType());
        }
    }

    @Test
    public void test_twoSpacesAndVariablesColonThanWord() {
        StringBuilder text = new StringBuilder(" Variables:");
        StringBuilder d = new StringBuilder(" ").append(text);
        assertThat(rec.hasNext(d, 1, 0)).isTrue();
        RobotToken token = rec.next();
        assertThat(token.getStartColumn()).isEqualTo(1);
        assertThat(token.getLineNumber()).isEqualTo(1);
        assertThat(token.getEndColumn()).isEqualTo(d.length());
        assertThat(token.getText().toString()).isEqualTo(text.toString());
        assertThat(token.getTypes()).containsExactly(rec.getProducedType());
    }

    @Test
    public void test_singleSpaceAndVariablesColonThanWord() {
        StringBuilder text = new StringBuilder(" Variables:");
        StringBuilder d = new StringBuilder(text).append("C");

        assertThat(rec.hasNext(d, 1, 0)).isTrue();
        RobotToken token = rec.next();
        assertThat(token.getStartColumn()).isEqualTo(0);
        assertThat(token.getLineNumber()).isEqualTo(1);
        assertThat(token.getEndColumn()).isEqualTo(text.length());
        assertThat(token.getText().toString()).isEqualTo(text.toString());
        assertThat(token.getTypes()).containsExactly(rec.getProducedType());
    }

    @Test
    public void test_singleVariablesColonThanLetterCWord() {
        StringBuilder text = new StringBuilder("Variables:");
        StringBuilder d = new StringBuilder(text).append("C");

        assertThat(rec.hasNext(d, 1, 0)).isTrue();
        RobotToken token = rec.next();
        assertThat(token.getStartColumn()).isEqualTo(0);
        assertThat(token.getLineNumber()).isEqualTo(1);
        assertThat(token.getEndColumn()).isEqualTo(text.length());
        assertThat(token.getText().toString()).isEqualTo(text.toString());
        assertThat(token.getTypes()).containsExactly(rec.getProducedType());
    }

    @Test
    public void test_singleVariablesColonWord() {
        StringBuilder text = new StringBuilder("Variables:");

        assertThat(rec.hasNext(text, 1, 0)).isTrue();
        RobotToken token = rec.next();
        assertThat(token.getStartColumn()).isEqualTo(0);
        assertThat(token.getLineNumber()).isEqualTo(1);
        assertThat(token.getEndColumn()).isEqualTo(text.length());
        assertThat(token.getText().toString()).isEqualTo(text.toString());
        assertThat(token.getTypes()).containsExactly(rec.getProducedType());
    }

    @Test
    public void test_variablesWord_allCombinations() {
        List<String> combinations = new CombinationGenerator().combinations("Variables");

        for (String comb : combinations) {
            StringBuilder textOfHeader = new StringBuilder(comb);

            assertThat(rec.hasNext(textOfHeader, 1, 0)).isTrue();
            RobotToken token = rec.next();
            assertThat(token.getStartColumn()).isEqualTo(0);
            assertThat(token.getLineNumber()).isEqualTo(1);
            assertThat(token.getEndColumn()).isEqualTo(textOfHeader.length());
            assertThat(token.getText().toString()).isEqualTo(textOfHeader.toString());
            assertThat(token.getTypes()).containsExactly(rec.getProducedType());
        }
    }

    @Test
    public void test_twoSpacesAndVariablesThanWord() {
        StringBuilder text = new StringBuilder(" Variables");
        StringBuilder d = new StringBuilder(" ").append(text);
        assertThat(rec.hasNext(d, 1, 0)).isTrue();
        RobotToken token = rec.next();
        assertThat(token.getStartColumn()).isEqualTo(1);
        assertThat(token.getLineNumber()).isEqualTo(1);
        assertThat(token.getEndColumn()).isEqualTo(d.length());
        assertThat(token.getText().toString()).isEqualTo(text.toString());
        assertThat(token.getTypes()).containsExactly(rec.getProducedType());
    }

    @Test
    public void test_singleSpaceAndVariablesThanWord() {
        StringBuilder text = new StringBuilder(" Variables");
        StringBuilder d = new StringBuilder(text).append("C");

        assertThat(rec.hasNext(d, 1, 0)).isTrue();
        RobotToken token = rec.next();
        assertThat(token.getStartColumn()).isEqualTo(0);
        assertThat(token.getLineNumber()).isEqualTo(1);
        assertThat(token.getEndColumn()).isEqualTo(text.length());
        assertThat(token.getText().toString()).isEqualTo(text.toString());
        assertThat(token.getTypes()).containsExactly(rec.getProducedType());
    }

    @Test
    public void test_singleVariablesThanLetterCWord() {
        StringBuilder text = new StringBuilder("Variables");
        StringBuilder d = new StringBuilder(text).append("C");

        assertThat(rec.hasNext(d, 1, 0)).isTrue();
        RobotToken token = rec.next();
        assertThat(token.getStartColumn()).isEqualTo(0);
        assertThat(token.getLineNumber()).isEqualTo(1);
        assertThat(token.getEndColumn()).isEqualTo(text.length());
        assertThat(token.getText().toString()).isEqualTo(text.toString());
        assertThat(token.getTypes()).containsExactly(rec.getProducedType());
    }

    @Test
    public void test_singleVariablesWord() {
        StringBuilder text = new StringBuilder("Variables");

        assertThat(rec.hasNext(text, 1, 0)).isTrue();
        RobotToken token = rec.next();
        assertThat(token.getStartColumn()).isEqualTo(0);
        assertThat(token.getLineNumber()).isEqualTo(1);
        assertThat(token.getEndColumn()).isEqualTo(text.length());
        assertThat(token.getText().toString()).isEqualTo(text.toString());
        assertThat(token.getTypes()).containsExactly(rec.getProducedType());
    }

    @Test
    public void test_getPattern() {
        assertThat(rec.getPattern().pattern())
                .isEqualTo("[ ]?(" + ATokenRecognizer.createUpperLowerCaseWord("Variables") + "[\\s]*:" + "|"
                        + ATokenRecognizer.createUpperLowerCaseWord("Variables") + ")");

    }

    @Before
    public void setUp() {
        rec = new VariableDeclarationRecognizer();
    }

    @After
    public void tearDown() throws Exception {
        ClassFieldCleaner.init(this);
    }
}
