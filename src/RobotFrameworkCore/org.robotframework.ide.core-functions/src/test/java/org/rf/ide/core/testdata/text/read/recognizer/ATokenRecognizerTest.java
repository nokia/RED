/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.read.recognizer;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.regex.Pattern;

import org.junit.Test;
import org.rf.ide.core.test.helpers.CombinationGenerator;

public class ATokenRecognizerTest {

    @Test
    public void test_hasNextAndNextGetting_twoTimesForFoobar() {
        // prepare
        final Pattern p = Pattern.compile("foobar");
        final RobotTokenType type = RobotTokenType.EMPTY_CELL;
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
        final RobotTokenType type = RobotTokenType.EMPTY_CELL;
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
        for (final String comb : new CombinationGenerator().combinations("foobar1")) {
            assertThat(ATokenRecognizer.createUpperLowerCaseWord(comb)).isEqualTo("[Ff][Oo][Oo][Bb][Aa][Rr]1");
        }
    }

    @Test
    public void test_createUpperLowerCaseWord_textIs1FoobarAllPossibleCombinations() {
        for (final String comb : new CombinationGenerator().combinations("1foobar")) {
            assertThat(ATokenRecognizer.createUpperLowerCaseWord(comb)).isEqualTo("1[Ff][Oo][Oo][Bb][Aa][Rr]");
        }
    }

    @Test
    public void test_createUpperLowerCaseWord_textIsFoo1barAllPossibleCombinations() {
        for (final String comb : new CombinationGenerator().combinations("foo1bar")) {
            assertThat(ATokenRecognizer.createUpperLowerCaseWord(comb)).isEqualTo("[Ff][Oo][Oo]1[Bb][Aa][Rr]");
        }
    }

    @Test
    public void test_createUpperLowerCaseWord_textIsFoobarAllPossibleCombinations() {
        for (final String comb : new CombinationGenerator().combinations("foobar")) {
            assertThat(ATokenRecognizer.createUpperLowerCaseWord(comb)).isEqualTo("[Ff][Oo][Oo][Bb][Aa][Rr]");
        }
    }

    @Test
    public void test_createUpperLowerCaseWord_textIsThreeTimesAmpersand() {
        assertThat(ATokenRecognizer.createUpperLowerCaseWord("&&&")).isEqualTo("&&&");
    }

    @Test
    public void test_createUpperLowerCaseWord_textIsAmpersand() {
        assertThat(ATokenRecognizer.createUpperLowerCaseWord("&")).isEqualTo("&");
    }

    @Test
    public void test_createUpperLowerCaseWord_textIsThreeTimesNumber() {
        assertThat(ATokenRecognizer.createUpperLowerCaseWord("111")).isEqualTo("111");
    }

    @Test
    public void test_createUpperLowerCaseWord_textIsNumber() {
        assertThat(ATokenRecognizer.createUpperLowerCaseWord("1")).isEqualTo("1");
    }

    @Test
    public void test_createUpperLowerCaseWord_textIsSpace() {
        assertThat(ATokenRecognizer.createUpperLowerCaseWord(" ")).isEqualTo(" ");
    }

    @Test
    public void test_createUpperLowerCaseWord_textIsEmpty() {
        assertThat(ATokenRecognizer.createUpperLowerCaseWord("")).isEmpty();
    }

    @Test
    public void test_createUpperLowerCaseWord_textIsNull() {
        assertThat(ATokenRecognizer.createUpperLowerCaseWord(null)).isEmpty();
    }

    @Test
    public void test_retrieveTypeAndPattern() {
        // prepare
        final Pattern p = Pattern.compile("foobar");
        final RobotTokenType type = RobotTokenType.EMPTY_CELL;
        final ATokenRecognizer rec = new DummyTokenRecognizer(p, type);

        // execute & verify
        assertThat(rec.getProducedType()).isEqualTo(type);
        assertThat(rec.getPattern()).isEqualTo(p);
    }

    @Test
    public void test_getProducedType() {
        // prepare
        final Pattern p = Pattern.compile("foobar");
        final RobotTokenType type = RobotTokenType.EMPTY_CELL;
        final ATokenRecognizer rec = new DummyTokenRecognizer(p, type);

        // execute & verify
        assertThat(rec.getProducedType()).isEqualTo(type);
    }

    @Test
    public void test_getPattern() {
        // prepare
        final Pattern p = Pattern.compile("foobar");
        final RobotTokenType type = RobotTokenType.EMPTY_CELL;
        final ATokenRecognizer rec = new DummyTokenRecognizer(p, type);

        // execute & verify
        assertThat(rec.getPattern()).isEqualTo(p);
    }

    private static class DummyTokenRecognizer extends ATokenRecognizer {

        public DummyTokenRecognizer(final Pattern p, final RobotTokenType type) {
            super(p, type);
        }

        @Override
        public ATokenRecognizer newInstance() {
            // added only for compile
            return null;
        }
    }
}
