/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.write;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.rf.ide.core.test.helpers.ClassFieldCleaner;
import org.rf.ide.core.test.helpers.ClassFieldCleaner.ForClean;
import org.rf.ide.core.testdata.model.FilePosition;
import org.rf.ide.core.testdata.text.write.DumperTestHelper.TextCompareResult;

public class DumperTestHelperUnitTest {

    @ForClean
    private DumperTestHelper helper;

    @Before
    public void setUp() {
        this.helper = new DumperTestHelper();
    }

    @After
    public void tearDown() throws Exception {
        ClassFieldCleaner.init(this);
    }

    @Test
    public void test_compare_theFirstTextParameterLengthIsGreater_thanTheSecondOne_differenceInCommonLengthPart() {
        // prepare
        final String text1 = "\r\nOcD";
        final String text2 = "\r\nOc";

        // execute
        final TextCompareResult cmpResult = helper.compare(text1, text2);

        // verify
        assertThat(cmpResult.expected()).isEqualTo(text1);
        assertThat(cmpResult.got()).isEqualTo(text2);
        assertThat(cmpResult.getDifferenceInExpected().isSamePlace(new FilePosition(2, 2, 4))).isTrue();
        assertThat(cmpResult.getDifferenceInGot().isSamePlace(new FilePosition(2, 2, 4))).isTrue();
    }

    @Test
    public void test_compare_theFirstTextParameterLengthIsGreater_thanTheSecondOne() {
        // prepare
        final String text1 = "\r\nOcD";
        final String text2 = "\r\nOc";

        // execute
        final TextCompareResult cmpResult = helper.compare(text1, text2);

        // verify
        assertThat(cmpResult.expected()).isEqualTo(text1);
        assertThat(cmpResult.got()).isEqualTo(text2);
        assertThat(cmpResult.getDifferenceInExpected().isSamePlace(new FilePosition(2, 2, 4))).isTrue();
        assertThat(cmpResult.getDifferenceInGot().isSamePlace(new FilePosition(2, 2, 4))).isTrue();
    }

    @Test
    public void test_compare_theSecondTextParameterLengthIsGreater_thanTheFirstOne_differenceInCommonLengthPart() {
        // prepare
        final String text1 = "\r\nOhD";
        final String text2 = "\r\nOc";

        // execute
        final TextCompareResult cmpResult = helper.compare(text1, text2);

        // verify
        assertThat(cmpResult.expected()).isEqualTo(text1);
        assertThat(cmpResult.got()).isEqualTo(text2);
        assertThat(cmpResult.getDifferenceInExpected().isSamePlace(new FilePosition(2, 1, 3))).isTrue();
        assertThat(cmpResult.getDifferenceInGot().isSamePlace(new FilePosition(2, 1, 3))).isTrue();
    }

    @Test
    public void test_compare_theSecondTextParameterLengthIsGreater_thanTheFirstOne() {
        // prepare
        final String text1 = "\r\nOcD";
        final String text2 = "\r\nOcDR";

        // execute
        final TextCompareResult cmpResult = helper.compare(text1, text2);

        // verify
        assertThat(cmpResult.expected()).isEqualTo(text1);
        assertThat(cmpResult.got()).isEqualTo(text2);
        assertThat(cmpResult.getDifferenceInExpected().isSamePlace(new FilePosition(2, 3, 5))).isTrue();
        assertThat(cmpResult.getDifferenceInGot().isSamePlace(new FilePosition(2, 3, 5))).isTrue();
    }

    @Test
    public void test_compare_textLength_isEqual_textAreDifferent() {
        // prepare
        final String text1 = "\r\nO1K";
        final String text2 = "\r\nOcK";

        // execute
        final TextCompareResult cmpResult = helper.compare(text1, text2);

        // verify
        assertThat(cmpResult.expected()).isEqualTo(text1);
        assertThat(cmpResult.got()).isEqualTo(text2);
        assertThat(cmpResult.getDifferenceInExpected().isSamePlace(new FilePosition(2, 1, 3))).isTrue();
        assertThat(cmpResult.getDifferenceInGot().isSamePlace(new FilePosition(2, 1, 3))).isTrue();
    }

    @Test
    public void test_compare_textLength_isEqual_textAreTheSame() {
        // prepare
        final String text1 = "\r\nOK";
        final String text2 = "\r\nOK";

        // execute
        final TextCompareResult cmpResult = helper.compare(text1, text2);

        // verify
        assertThat(cmpResult.expected()).isEqualTo(text1);
        assertThat(cmpResult.got()).isEqualTo(text2);
        assertThat(cmpResult.getDifferenceInExpected().isSamePlace(FilePosition.createNotSet())).isTrue();
        assertThat(cmpResult.getDifferenceInGot().isSamePlace(FilePosition.createNotSet())).isTrue();
    }
}
