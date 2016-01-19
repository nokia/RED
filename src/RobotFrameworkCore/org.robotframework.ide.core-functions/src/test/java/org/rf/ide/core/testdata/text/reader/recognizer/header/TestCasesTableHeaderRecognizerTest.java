/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.reader.recognizer.header;

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
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;
import org.rf.ide.core.testdata.text.read.recognizer.header.TestCasesTableHeaderRecognizer;

@SuppressWarnings({ "PMD.MethodNamingConventions", "PMD.TooManyMethods" })
public class TestCasesTableHeaderRecognizerTest {

    @ForClean
    private ATokenRecognizer rec;

    @Test
    public void test_check_TestCasesAllPossibilities_withAsterisks_atTheBeginAndEnd() {
        assertAllCombinations("Test Cases");
    }

    @Test
    public void test_check_TestCaseAllPossibilities_withAsterisks_atTheBeginAndEnd() {
        assertAllCombinations("Test Case");
    }

    private void assertAllCombinations(String text) {
        List<String> combinations = new CombinationGenerator().combinations(text);

        for (String comb : combinations) {
            StringBuilder textOfHeader = new StringBuilder("*** ").append(comb).append(" ***");

            assertThat(rec.hasNext(textOfHeader, 1)).isTrue();
            RobotToken token = rec.next();
            assertThat(token.getStartColumn()).isEqualTo(0);
            assertThat(token.getLineNumber()).isEqualTo(1);
            assertThat(token.getEndColumn()).isEqualTo(textOfHeader.length());
            assertThat(token.getText().toString()).isEqualTo(textOfHeader.toString());
            assertThat(token.getTypes()).containsExactly(rec.getProducedType());
        }
    }

    @Test
    public void test_check_TestCases_withAsterisk_atTheBeginAndEnd_spaceLetterT() {
        String expectedToCut = " *  Test Cases *";
        StringBuilder text = new StringBuilder(expectedToCut).append(" T");

        assertThat(rec.hasNext(text, 1)).isTrue();
        RobotToken token = rec.next();
        assertThat(token.getStartColumn()).isEqualTo(0);
        assertThat(token.getLineNumber()).isEqualTo(1);
        assertThat(token.getEndColumn()).isEqualTo(expectedToCut.length());
        assertThat(token.getText().toString()).isEqualTo(expectedToCut);
        assertThat(token.getTypes()).containsExactly(rec.getProducedType());
    }

    @Test
    public void test_check_spaceLetterT_and_TestCases_withAsterisk_atTheBeginAndEnd() {
        StringBuilder text = new StringBuilder("T *  Test Cases ***");

        assertThat(rec.hasNext(text, 1)).isTrue();
        RobotToken token = rec.next();
        assertThat(token.getStartColumn()).isEqualTo(1);
        assertThat(token.getLineNumber()).isEqualTo(1);
        assertThat(token.getEndColumn()).isEqualTo(text.length());
        assertThat(token.getText().toString()).isEqualTo(" *  Test Cases ***");
        assertThat(token.getTypes()).containsExactly(rec.getProducedType());
    }

    @Test
    public void test_check_spaceTestCases_withAsterisk_atTheBeginAndEnd() {
        StringBuilder text = new StringBuilder(" *  Test Cases ***");

        assertThat(rec.hasNext(text, 1)).isTrue();
        RobotToken token = rec.next();
        assertThat(token.getStartColumn()).isEqualTo(0);
        assertThat(token.getLineNumber()).isEqualTo(1);
        assertThat(token.getEndColumn()).isEqualTo(text.length());
        assertThat(token.getText().toString()).isEqualTo(text.toString());
        assertThat(token.getTypes()).containsExactly(rec.getProducedType());
    }

    @Test
    public void test_check_TestCases_withAsterisk_atTheBeginAndEnd() {
        StringBuilder text = new StringBuilder("*  Test Cases ***");

        assertThat(rec.hasNext(text, 1)).isTrue();
        RobotToken token = rec.next();
        assertThat(token.getStartColumn()).isEqualTo(0);
        assertThat(token.getLineNumber()).isEqualTo(1);
        assertThat(token.getEndColumn()).isEqualTo(text.length());
        assertThat(token.getText().toString()).isEqualTo(text.toString());
        assertThat(token.getTypes()).containsExactly(rec.getProducedType());
    }

    @Test
    public void test_check_TestCases_withAsterisks_atTheBeginAndEnd_spaceLetterT() {
        String expectedToCut = " ***  Test Cases ***";
        StringBuilder text = new StringBuilder(expectedToCut).append(" T");

        assertThat(rec.hasNext(text, 1)).isTrue();
        RobotToken token = rec.next();
        assertThat(token.getStartColumn()).isEqualTo(0);
        assertThat(token.getLineNumber()).isEqualTo(1);
        assertThat(token.getEndColumn()).isEqualTo(expectedToCut.length());
        assertThat(token.getText().toString()).isEqualTo(expectedToCut);
        assertThat(token.getTypes()).containsExactly(rec.getProducedType());
    }

    @Test
    public void test_check_spaceLetterT_and_TestCases_withAsterisks_atTheBeginAndEnd() {
        StringBuilder text = new StringBuilder("T ***  Test Cases ***");

        assertThat(rec.hasNext(text, 1)).isTrue();
        RobotToken token = rec.next();
        assertThat(token.getStartColumn()).isEqualTo(1);
        assertThat(token.getLineNumber()).isEqualTo(1);
        assertThat(token.getEndColumn()).isEqualTo(text.length());
        assertThat(token.getText().toString()).isEqualTo(" ***  Test Cases ***");
        assertThat(token.getTypes()).containsExactly(rec.getProducedType());
    }

    @Test
    public void test_check_spaceTestCases_withAsterisks_atTheBeginAndEnd() {
        StringBuilder text = new StringBuilder(" ***  Test Cases ***");

        assertThat(rec.hasNext(text, 1)).isTrue();
        RobotToken token = rec.next();
        assertThat(token.getStartColumn()).isEqualTo(0);
        assertThat(token.getLineNumber()).isEqualTo(1);
        assertThat(token.getEndColumn()).isEqualTo(text.length());
        assertThat(token.getText().toString()).isEqualTo(text.toString());
        assertThat(token.getTypes()).containsExactly(rec.getProducedType());
    }

    @Test
    public void test_check_TestCases_withAsterisks_atTheBeginAndEnd() {
        StringBuilder text = new StringBuilder("***  Test Cases ***");

        assertThat(rec.hasNext(text, 1)).isTrue();
        RobotToken token = rec.next();
        assertThat(token.getStartColumn()).isEqualTo(0);
        assertThat(token.getLineNumber()).isEqualTo(1);
        assertThat(token.getEndColumn()).isEqualTo(text.length());
        assertThat(token.getText().toString()).isEqualTo(text.toString());
        assertThat(token.getTypes()).containsExactly(rec.getProducedType());
    }

    @Test
    public void test_check_TestCase_withAsterisk_atTheBeginAndEnd_spaceLetterT() {
        String expectedToCut = " * Test Case *";
        StringBuilder text = new StringBuilder(expectedToCut).append(" T");

        assertThat(rec.hasNext(text, 1)).isTrue();
        RobotToken token = rec.next();
        assertThat(token.getStartColumn()).isEqualTo(0);
        assertThat(token.getLineNumber()).isEqualTo(1);
        assertThat(token.getEndColumn()).isEqualTo(expectedToCut.length());
        assertThat(token.getText().toString()).isEqualTo(expectedToCut);
        assertThat(token.getTypes()).containsExactly(rec.getProducedType());
    }

    @Test
    public void test_check_spaceLetterT_and_TestCase_withAsterisk_atTheBeginAndEnd() {
        StringBuilder text = new StringBuilder("T * Test Case ***");

        assertThat(rec.hasNext(text, 1)).isTrue();
        RobotToken token = rec.next();
        assertThat(token.getStartColumn()).isEqualTo(1);
        assertThat(token.getLineNumber()).isEqualTo(1);
        assertThat(token.getEndColumn()).isEqualTo(text.length());
        assertThat(token.getText().toString()).isEqualTo(" * Test Case ***");
        assertThat(token.getTypes()).containsExactly(rec.getProducedType());
    }

    @Test
    public void test_check_spaceTestCase_withAsterisk_atTheBeginAndEnd() {
        StringBuilder text = new StringBuilder(" * Test Case ***");

        assertThat(rec.hasNext(text, 1)).isTrue();
        RobotToken token = rec.next();
        assertThat(token.getStartColumn()).isEqualTo(0);
        assertThat(token.getLineNumber()).isEqualTo(1);
        assertThat(token.getEndColumn()).isEqualTo(text.length());
        assertThat(token.getText().toString()).isEqualTo(text.toString());
        assertThat(token.getTypes()).containsExactly(rec.getProducedType());
    }

    @Test
    public void test_check_TestCase_withAsterisk_atTheBeginAndEnd() {
        StringBuilder text = new StringBuilder("* Test Case ***");

        assertThat(rec.hasNext(text, 1)).isTrue();
        RobotToken token = rec.next();
        assertThat(token.getStartColumn()).isEqualTo(0);
        assertThat(token.getLineNumber()).isEqualTo(1);
        assertThat(token.getEndColumn()).isEqualTo(text.length());
        assertThat(token.getText().toString()).isEqualTo(text.toString());
        assertThat(token.getTypes()).containsExactly(rec.getProducedType());
    }

    @Test
    public void test_check_TestCase_withAsterisks_atTheBeginAndEnd_spaceLetterT() {
        String expectedToCut = " *** Test Case ***";
        StringBuilder text = new StringBuilder(expectedToCut).append(" T");

        assertThat(rec.hasNext(text, 1)).isTrue();
        RobotToken token = rec.next();
        assertThat(token.getStartColumn()).isEqualTo(0);
        assertThat(token.getLineNumber()).isEqualTo(1);
        assertThat(token.getEndColumn()).isEqualTo(expectedToCut.length());
        assertThat(token.getText().toString()).isEqualTo(expectedToCut);
        assertThat(token.getTypes()).containsExactly(rec.getProducedType());
    }

    @Test
    public void test_check_spaceLetterT_and_TestCase_withAsterisks_atTheBeginAndEnd() {
        StringBuilder text = new StringBuilder("T *** Test Case ***");

        assertThat(rec.hasNext(text, 1)).isTrue();
        RobotToken token = rec.next();
        assertThat(token.getStartColumn()).isEqualTo(1);
        assertThat(token.getLineNumber()).isEqualTo(1);
        assertThat(token.getEndColumn()).isEqualTo(text.length());
        assertThat(token.getText().toString()).isEqualTo(" *** Test Case ***");
        assertThat(token.getTypes()).containsExactly(rec.getProducedType());
    }

    @Test
    public void test_check_spaceTestCase_withAsterisks_atTheBeginAndEnd() {
        StringBuilder text = new StringBuilder(" *** Test Case ***");

        assertThat(rec.hasNext(text, 1)).isTrue();
        RobotToken token = rec.next();
        assertThat(token.getStartColumn()).isEqualTo(0);
        assertThat(token.getLineNumber()).isEqualTo(1);
        assertThat(token.getEndColumn()).isEqualTo(text.length());
        assertThat(token.getText().toString()).isEqualTo(text.toString());
        assertThat(token.getTypes()).containsExactly(rec.getProducedType());
    }

    @Test
    public void test_check_TestCase_withAsterisks_atTheBeginAndEnd() {
        StringBuilder text = new StringBuilder("*** Test Case ***");

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
        assertThat(rec.getPattern().pattern())
                .isEqualTo("[ ]?([*][\\s]*)+[\\s]*" + ATokenRecognizer.createUpperLowerCaseWordWithSpacesInside("Test")
                        + "([\\s]+)?(" + ATokenRecognizer.createUpperLowerCaseWordWithSpacesInside("Cases") + "|"
                        + ATokenRecognizer.createUpperLowerCaseWordWithSpacesInside("Case") + ")([\\s]*[*])*");
    }

    @Test
    public void test_getProducedType() {
        assertThat(rec.getProducedType()).isEqualTo(RobotTokenType.TEST_CASES_TABLE_HEADER);
    }

    @Before
    public void setUp() {
        rec = new TestCasesTableHeaderRecognizer();
    }

    @After
    public void tearDown() throws Exception {
        ClassFieldCleaner.init(this);
    }
}
