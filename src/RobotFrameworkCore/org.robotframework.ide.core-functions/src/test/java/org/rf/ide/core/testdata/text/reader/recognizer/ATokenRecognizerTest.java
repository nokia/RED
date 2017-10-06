/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.reader.recognizer;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.regex.Pattern;

import org.junit.After;
import org.junit.Test;
import org.rf.ide.core.test.helpers.ClassFieldCleaner;
import org.rf.ide.core.test.helpers.CombinationGenerator;
import org.rf.ide.core.testdata.text.read.recognizer.ATokenRecognizer;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

@SuppressWarnings("PMD.MethodNamingConventions")
public class ATokenRecognizerTest {

    @Test
    public void test_hasNextAndNextGetting_twoTimesForFoobar() {
        // prepare
        final Pattern p = Pattern.compile("foobar");
        final RobotTokenType type = RobotTokenType.TEST_CASE_EMPTY_CELL;
        final ATokenRecognizer rec = new DummyTokenRecognizer(p, type);
        final StringBuilder text = new StringBuilder("foobarfoobar");

        // execute & verify
        assertThat(rec.hasNext(text, 0, 0)).isTrue();
        final RobotToken one = rec.next();
        assertThat(one.getStartColumn()).isEqualTo(0);
        assertThat(one.getText().toString()).isEqualTo("foobar");
        assertThat(one.getEndColumn()).isEqualTo("foobar".length());
        assertThat(one.getTypes()).containsExactly(type);
        assertThat(rec.hasNext(text, 0, 0)).isTrue();

        final RobotToken two = rec.next();
        assertThat(two.getStartColumn()).isEqualTo(6);
        assertThat(two.getText().toString()).isEqualTo("foobar");
        assertThat(two.getEndColumn()).isEqualTo(6 + "foobar".length());
        assertThat(two.getTypes()).containsExactly(type);
    }

    @Test
    public void test_hasNextAndNextGetting_oneTimeForFoobar() {
        // prepare
        final Pattern p = Pattern.compile("foobar");
        final RobotTokenType type = RobotTokenType.TEST_CASE_EMPTY_CELL;
        final ATokenRecognizer rec = new DummyTokenRecognizer(p, type);
        final StringBuilder text = new StringBuilder("foobar");

        // execute & verify
        assertThat(rec.hasNext(text, 0, 0)).isTrue();
        final RobotToken one = rec.next();
        assertThat(one.getStartColumn()).isEqualTo(0);
        assertThat(one.getText().toString()).isEqualTo(text.toString());
        assertThat(one.getEndColumn()).isEqualTo(text.length());
        assertThat(one.getTypes()).containsExactly(type);
        assertThat(rec.hasNext(new StringBuilder(""), 0, 0)).isFalse();
    }

    @Test
    public void test_createUpperLowerCaseWord_textIsFoobar1AllPossibleCombinations() {
        // prepare
        final String text = "foobar1";

        // verify & execute
        final List<String> combinations = new CombinationGenerator().combinations(text);
        for (final String comb : combinations) {
            assertThat(ATokenRecognizer.createUpperLowerCaseWord(comb)).isEqualTo("[F|f][O|o][O|o][B|b][A|a][R|r][1]");
        }
    }

    @Test
    public void test_createUpperLowerCaseWord_textIs1FoobarAllPossibleCombinations() {
        // prepare
        final String text = "1foobar";

        // verify & execute
        final List<String> combinations = new CombinationGenerator().combinations(text);
        for (final String comb : combinations) {
            assertThat(ATokenRecognizer.createUpperLowerCaseWord(comb)).isEqualTo("[1][F|f][O|o][O|o][B|b][A|a][R|r]");
        }
    }

    @Test
    public void test_createUpperLowerCaseWord_textIsFoo1barAllPossibleCombinations() {
        // prepare
        final String text = "foo1bar";

        // verify & execute
        final List<String> combinations = new CombinationGenerator().combinations(text);
        for (final String comb : combinations) {
            assertThat(ATokenRecognizer.createUpperLowerCaseWord(comb)).isEqualTo("[F|f][O|o][O|o][1][B|b][A|a][R|r]");
        }
    }

    @Test
    public void test_createUpperLowerCaseWord_textIsFoobarAllPossibleCombinations() {
        // prepare
        final String text = "foobar";

        // verify & execute
        final List<String> combinations = new CombinationGenerator().combinations(text);
        for (final String comb : combinations) {
            assertThat(ATokenRecognizer.createUpperLowerCaseWord(comb)).isEqualTo("[F|f][O|o][O|o][B|b][A|a][R|r]");
        }
    }

    @Test
    public void test_createUpperLowerCaseWord_textIsFoobar() {
        // prepare
        final String text = "foobar";

        // verify & execute
        assertThat(ATokenRecognizer.createUpperLowerCaseWord(text)).isEqualTo("[F|f][O|o][O|o][B|b][A|a][R|r]");
    }

    @Test
    public void test_createUpperLowerCaseWord_textIsThreeTimesAmpersand() {
        // prepare
        final String text = "&&&";
        final String expectedOneIteration = "[&]";

        // verify & execute
        assertThat(ATokenRecognizer.createUpperLowerCaseWord(text))
                .isEqualTo(expectedOneIteration + expectedOneIteration + expectedOneIteration);
    }

    @Test
    public void test_createUpperLowerCaseWord_textIsAmpersand() {
        // prepare
        final String text = "&";

        // verify & execute
        assertThat(ATokenRecognizer.createUpperLowerCaseWord(text)).isEqualTo("[" + text + "]");
    }

    @Test
    public void test_createUpperLowerCaseWord_textIsThreeTimesNumber() {
        // prepare
        final String text = "111";
        final String expectedOneIteration = "[1]";

        // verify & execute
        assertThat(ATokenRecognizer.createUpperLowerCaseWord(text))
                .isEqualTo(expectedOneIteration + expectedOneIteration + expectedOneIteration);
    }

    @Test
    public void test_createUpperLowerCaseWord_textIsNumber() {
        // prepare
        final String text = "1";

        // verify & execute
        assertThat(ATokenRecognizer.createUpperLowerCaseWord(text)).isEqualTo("[" + text + "]");
    }

    @Test
    public void test_createUpperLowerCaseWord_textIsSpace() {
        // prepare
        final String text = " ";

        // verify & execute
        assertThat(ATokenRecognizer.createUpperLowerCaseWord(text)).isEqualTo("[" + text + "]");
    }

    @Test
    public void test_createUpperLowerCaseWord_textIsEmpty() {
        // prepare
        final String text = "";

        // verify & execute
        assertThat(ATokenRecognizer.createUpperLowerCaseWord(text)).isEmpty();
    }

    @Test
    public void test_createUpperLowerCaseWord_textIsNull() {
        // prepare
        final String text = null;

        // verify & execute
        assertThat(ATokenRecognizer.createUpperLowerCaseWord(text)).isEmpty();
    }

    @Test
    public void test_retrieveTypeAndPattern() {
        // prepare
        final Pattern p = Pattern.compile("foobar");
        final RobotTokenType type = RobotTokenType.TEST_CASE_EMPTY_CELL;
        final ATokenRecognizer rec = new DummyTokenRecognizer(p, type);

        // execute & verify
        assertThat(rec.getProducedType()).isEqualTo(type);
        assertThat(rec.getPattern()).isEqualTo(p);
    }

    @Test
    public void test_getProducedType() {
        // prepare
        final Pattern p = Pattern.compile("foobar");
        final RobotTokenType type = RobotTokenType.TEST_CASE_EMPTY_CELL;
        final ATokenRecognizer rec = new DummyTokenRecognizer(p, type);

        // execute & verify
        assertThat(rec.getProducedType()).isEqualTo(type);
    }

    @Test
    public void test_getPattern() {
        // prepare
        final Pattern p = Pattern.compile("foobar");
        final RobotTokenType type = RobotTokenType.TEST_CASE_EMPTY_CELL;
        final ATokenRecognizer rec = new DummyTokenRecognizer(p, type);

        // execute & verify
        assertThat(rec.getPattern()).isEqualTo(p);
    }

    @After
    public void tearDown() throws Exception {
        ClassFieldCleaner.init(this);
    }

    private static class DummyTokenRecognizer extends ATokenRecognizer {

        public DummyTokenRecognizer(Pattern p, RobotTokenType type) {
            super(p, type);
        }

        @Override
        public ATokenRecognizer newInstance() {
            // added only for compile
            return null;
        }
    }
}
