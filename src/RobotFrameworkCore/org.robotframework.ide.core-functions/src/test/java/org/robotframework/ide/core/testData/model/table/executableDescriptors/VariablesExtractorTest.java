/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.core.testData.model.table.executableDescriptors;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.robotframework.ide.core.testData.model.table.executableDescriptors.VariablesExtractor.TextPosition;
import org.robotframework.ide.core.testHelpers.ClassFieldCleaner;
import org.robotframework.ide.core.testHelpers.ClassFieldCleaner.ForClean;

import com.google.common.collect.LinkedListMultimap;


public class VariablesExtractorTest {

    @ForClean
    private VariablesExtractor varExtractor;


    @Test
    public void findCharsPositions_textIsFoobar_charListIsC_shouldReturnEmptyList() {
        LinkedListMultimap<Character, Integer> c = varExtractor
                .findCharsPositions("foobar", Arrays.asList('c'));
        assertThat(c.isEmpty()).isTrue();
    }


    @Test
    public void findCharsPositions_textIsFoobar_charListIsO_shouldReturnTwoPositions() {
        LinkedListMultimap<Character, Integer> c = varExtractor
                .findCharsPositions("foobar", Arrays.asList('o'));
        assertThat(c.keySet()).containsExactly('o');
        assertThat(c.values()).containsExactly(1, 2);
    }


    @Test
    public void findCharsPositions_textIsFoobar_charListIsEmpty() {
        LinkedListMultimap<Character, Integer> c = varExtractor
                .findCharsPositions("foobar", new LinkedList<Character>());
        assertThat(c.isEmpty()).isTrue();
    }


    @Test
    public void findCharsPositions_textIsNull_charListIsEmpty() {
        LinkedListMultimap<Character, Integer> c = varExtractor
                .findCharsPositions(null, new LinkedList<Character>());
        assertThat(c.isEmpty()).isTrue();
    }


    @Test
    public void findCharsPositions_textIsNull_charListIsNotEmpty() {
        LinkedListMultimap<Character, Integer> c = varExtractor
                .findCharsPositions(null, Arrays.asList('|'));
        assertThat(c.isEmpty()).isTrue();
    }


    @Test
    public void extractPossibleVariablePositions_environmentAndScalarAndDictionaryAndList_shouldReturn_fourElements() {
        Object[] expectedText = new Object[] { "%{env", "${scalar", "&{dict",
                "@{list" };
        String template = "nowy %s} %s[ %s[ %s";
        assertPossibleVariablesExtraction(template, expectedText);
    }


    @Test
    public void extractPossibleVariablePositions_scalarAndDictionaryAndList_shouldReturn_threeElements() {
        Object[] expectedText = new Object[] { "${scalar", "&{dict", "@{list" };
        String template = "nowy %s[ %s[ %s";
        assertPossibleVariablesExtraction(template, expectedText);
    }


    @Test
    public void extractPossibleVariablePositions_scalarAndDictionary_shouldReturn_twoElements() {
        Object[] expectedText = new Object[] { "${scalar", "&{dict" };
        String template = "nowy %s[ %s";
        assertPossibleVariablesExtraction(template, expectedText);
    }


    private void assertPossibleVariablesExtraction(String template,
            Object[] expectedText) {
        String robotArgument = String.format(template, expectedText);
        List<TextPosition> extractPossibleVariablePositions = varExtractor
                .extractPossibleVariablePositions(robotArgument);
        int length = expectedText.length;
        assertThat(extractPossibleVariablePositions).hasSize(length);
        for (int possibleVarIndex = 0; possibleVarIndex < length; possibleVarIndex++) {
            TextPosition pVariablePosition = extractPossibleVariablePositions
                    .get(possibleVarIndex);
            assertThat(
                    robotArgument.substring(pVariablePosition.getStart(),
                            pVariablePosition.getEnd())).isEqualTo(
                    expectedText[possibleVarIndex]);
        }
    }


    @Test
    public void extractPossibleVariablePositions_onlyEscapedEnvironment_shouldReturn_oneElement() {
        String prefix = "nowy ";
        String varStart = "\\%{{test";
        String robotArgument = prefix + varStart;
        assertVariablePositionWithOneElementOnly(prefix, varStart,
                robotArgument);
    }


    @Test
    public void extractPossibleVariablePositions_onlyEscapedDictionary_shouldReturn_oneElement() {
        String prefix = "nowy ";
        String varStart = "\\&{{test";
        String robotArgument = prefix + varStart;
        assertVariablePositionWithOneElementOnly(prefix, varStart,
                robotArgument);
    }


    @Test
    public void extractPossibleVariablePositions_onlyEscapedList_shouldReturn_oneElement() {
        String prefix = "nowy ";
        String varStart = "\\@{{test";
        String robotArgument = prefix + varStart;
        assertVariablePositionWithOneElementOnly(prefix, varStart,
                robotArgument);
    }


    @Test
    public void extractPossibleVariablePositions_onlyEscpedScalar_shouldReturn_oneElement() {
        String prefix = "nowy ";
        String varStart = "\\${test";
        String robotArgument = prefix + varStart;
        assertVariablePositionWithOneElementOnly(prefix, varStart,
                robotArgument);
    }


    @Test
    public void extractPossibleVariablePositions_onlyEnvironment_shouldReturn_oneElement() {
        String prefix = "nowy ";
        String varStart = "%{test";
        String robotArgument = prefix + varStart;
        assertVariablePositionWithOneElementOnly(prefix, varStart,
                robotArgument);
    }


    @Test
    public void extractPossibleVariablePositions_onlyDictionary_shouldReturn_oneElement() {
        String prefix = "nowy ";
        String varStart = "&{test";
        String robotArgument = prefix + varStart;
        assertVariablePositionWithOneElementOnly(prefix, varStart,
                robotArgument);
    }


    @Test
    public void extractPossibleVariablePositions_onlyList_shouldReturn_oneElement() {
        String prefix = "nowy ";
        String varStart = "@{test";
        String robotArgument = prefix + varStart;
        assertVariablePositionWithOneElementOnly(prefix, varStart,
                robotArgument);
    }


    @Test
    public void extractPossibleVariablePositions_onlyScalar_shouldReturn_oneElement() {
        String prefix = "nowy ";
        String varStart = "${test";
        String robotArgument = prefix + varStart;
        assertVariablePositionWithOneElementOnly(prefix, varStart,
                robotArgument);
    }


    private void assertVariablePositionWithOneElementOnly(String prefix,
            String varStart, String robotArgument) {
        List<TextPosition> extractPossibleVariablePositions = varExtractor
                .extractPossibleVariablePositions(robotArgument);
        assertThat(extractPossibleVariablePositions).hasSize(1);
        TextPosition variablePosition = extractPossibleVariablePositions.get(0);
        assertThat(variablePosition.getStart()).isEqualTo(prefix.length());
        assertThat(variablePosition.getEnd()).isEqualTo(
                prefix.length() + varStart.length());
    }


    @Test
    public void extractPossibleVariablePositions_noVariables_shouldReturn_emptyList() {
        assertThat(varExtractor.extractPossibleVariablePositions("{nowy}"))
                .isEmpty();
    }


    @Before
    public void setUp() throws Exception {
        ClassFieldCleaner.init(this);
        varExtractor = new VariablesExtractor();
    }
}
