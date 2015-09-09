/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see licence.txt file for details.
 */
package org.robotframework.ide.core.testData.text.read.recognizer.settings;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.robotframework.ide.core.testData.text.read.recognizer.ATokenRecognizer;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotToken;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotTokenType;
import org.robotframework.ide.core.testHelpers.ClassFieldCleaner;
import org.robotframework.ide.core.testHelpers.ClassFieldCleaner.ForClean;
import org.robotframework.ide.core.testHelpers.CombinationGenerator;


public class LibraryAliasRecognizerTest {

    @ForClean
    private ATokenRecognizer rec;


    @Test
    public void test_withNameWords_allCombinations() {
        List<String> combinations = new CombinationGenerator()
                .combinations("with name");

        for (String comb : combinations) {
            StringBuilder textOfHeader = new StringBuilder(comb);

            assertThat(rec.hasNext(textOfHeader, 1)).isTrue();
            RobotToken token = rec.next();
            assertThat(token.getStartColumn()).isEqualTo(0);
            assertThat(token.getLineNumber()).isEqualTo(1);
            assertThat(token.getEndColumn()).isEqualTo(textOfHeader.length());
            assertThat(token.getText().toString()).isEqualTo(
                    textOfHeader.toString());
            assertThat(token.getTypes()).containsExactly(rec.getProducedType());
        }
    }


    @Test
    public void test_twoSpacesAnd_WITH_NAME_ThanWord() {
        StringBuilder text = new StringBuilder(" WITH NAME");
        StringBuilder d = new StringBuilder(" ").append(text);
        assertThat(rec.hasNext(d, 1)).isTrue();
        RobotToken token = rec.next();
        assertThat(token.getStartColumn()).isEqualTo(2);
        assertThat(token.getLineNumber()).isEqualTo(1);
        assertThat(token.getEndColumn()).isEqualTo(d.length());
        assertThat(token.getText().toString()).isEqualTo(text.substring(1));
        assertThat(token.getTypes()).containsExactly(rec.getProducedType());
    }


    @Test
    public void test_singleSpaceAndWITH_NAMEThanWord() {
        StringBuilder text = new StringBuilder(" WITH NAME");
        StringBuilder d = new StringBuilder(text).append("C");

        assertThat(rec.hasNext(d, 1)).isTrue();
        RobotToken token = rec.next();
        assertThat(token.getStartColumn()).isEqualTo(1);
        assertThat(token.getLineNumber()).isEqualTo(1);
        assertThat(token.getEndColumn()).isEqualTo(text.length());
        assertThat(token.getText().toString()).isEqualTo(text.substring(1));
        assertThat(token.getTypes()).containsExactly(rec.getProducedType());
    }


    @Test
    public void test_singleAliasThanLetterCWord() {
        StringBuilder text = new StringBuilder("WITH NAME");
        StringBuilder d = new StringBuilder(text).append("C");

        assertThat(rec.hasNext(d, 1)).isTrue();
        RobotToken token = rec.next();
        assertThat(token.getStartColumn()).isEqualTo(0);
        assertThat(token.getLineNumber()).isEqualTo(1);
        assertThat(token.getEndColumn()).isEqualTo(text.length());
        assertThat(token.getText().toString()).isEqualTo(text.toString());
        assertThat(token.getTypes()).containsExactly(rec.getProducedType());
    }


    @Test
    public void test_singleAliasWord() {
        StringBuilder text = new StringBuilder("WITH NAME");

        assertThat(rec.hasNext(text, 1)).isTrue();
        RobotToken token = rec.next();
        assertThat(token.getStartColumn()).isEqualTo(0);
        assertThat(token.getLineNumber()).isEqualTo(1);
        assertThat(token.getEndColumn()).isEqualTo(text.length());
        assertThat(token.getText().toString()).isEqualTo(text.toString());
        assertThat(token.getTypes()).containsExactly(rec.getProducedType());
    }


    @Test
    public void test_getPattern() {
        assertThat(rec.getPattern().pattern()).isEqualTo(
                ATokenRecognizer.createUpperLowerCaseWord("WITH") + "[\\s]+"
                        + ATokenRecognizer.createUpperLowerCaseWord("NAME"));
    }


    @Test
    public void test_getProducedType() {
        assertThat(rec.getProducedType()).isEqualTo(
                RobotTokenType.SETTING_LIBRARY_ALIAS);
    }


    @Before
    public void setUp() {
        rec = new LibraryAliasRecognizer();
    }


    @After
    public void tearDown() throws Exception {
        ClassFieldCleaner.init(this);
    }
}
