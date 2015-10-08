/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.core.testData.text.read.recognizer.variables;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.robotframework.ide.core.testData.text.read.recognizer.variables.VariablesExtractor.TextualPosition;
import org.robotframework.ide.core.testHelpers.ClassFieldCleaner;
import org.robotframework.ide.core.testHelpers.ClassFieldCleaner.ForClean;

import com.google.common.collect.LinkedListMultimap;


public class VariablesExtractorTest {

    @ForClean
    private VariablesExtractor varExtractor;


    @Test
    public void getIndexOfNearestCurrlyBracket_twoElementsList() {
        TextualPosition varPosition = mock(TextualPosition.class);
        when(varPosition.getEndPosition()).thenReturn(10);
        List<Integer> currlyBracketPositions = Arrays.asList(5, 12, 20);

        int indexOfNearestCurrlyBracket = varExtractor
                .getIndexOfNearestCurrlyBracket(currlyBracketPositions,
                        varPosition);
        assertThat(indexOfNearestCurrlyBracket).isEqualTo(1);
        assertThat(currlyBracketPositions.get(indexOfNearestCurrlyBracket))
                .isEqualTo(12);
    }


    @Test
    public void getIndexOfNearestCurrlyBracket_emptyCurrlyBracketList_shouldReturn_minusOne() {
        TextualPosition varPosition = mock(TextualPosition.class);
        assertThat(
                varExtractor.getIndexOfNearestCurrlyBracket(
                        new LinkedList<Integer>(), varPosition)).isEqualTo(-1);
    }


    @Test
    public void getCharsPosition_textIsFoobar_listContainsOnlyCharD_shouldReturn_emptyList() {
        assertThat(
                varExtractor.getCharsPosition("foobar", Arrays.asList('D'))
                        .isEmpty()).isTrue();
    }


    @Test
    public void getCharsPosition_textIsFoobar_listContainsOnlyCharO_shouldReturn_twoElementsList() {
        LinkedListMultimap<Character, Integer> charsPosition = varExtractor
                .getCharsPosition("foobar", Arrays.asList('o'));
        assertThat(charsPosition.keySet()).containsExactly('o');
        assertThat(charsPosition.get('o')).containsExactly(1, 2);
    }


    @Test
    public void getCharsPosition_textIsFoobar_listToFindIsEmpty() {
        @SuppressWarnings("unchecked")
        List<Character> emptyList = mock(List.class);
        when(emptyList.isEmpty()).thenReturn(true);

        assertThat(varExtractor.getCharsPosition("foobar", emptyList).isEmpty())
                .isTrue();

        InOrder order = inOrder(emptyList);
        order.verify(emptyList, times(1)).isEmpty();
        order.verifyNoMoreInteractions();
    }


    @Test
    public void getCharsPosition_NULL_text_shouldReturn_emptyList() {
        assertThat(varExtractor.getCharsPosition(null, null).isEmpty())
                .isTrue();
    }


    @Test
    public void getVariablesStartPostion_twoScalars_closeToEachOther_shouldReturn_twoElementsList() {
        String text = "nowy ";
        String scalarOne = "${foobar} ";
        String scalarTwo = "${last}";
        String finalText = text + scalarOne + scalarTwo;

        List<TextualPosition> variables = varExtractor
                .getVariablesStartPosition(finalText);
        assertThat(variables).hasSize(2);
        TextualPosition varScalarOne = variables.get(0);
        assertThat(varScalarOne.getOriginalText()).isEqualTo(finalText);
        assertThat(varScalarOne.getStartPosition()).isEqualTo(text.length());
        assertThat(varScalarOne.getEndPosition()).isEqualTo(text.length() + 2);

        TextualPosition varScalarTwo = variables.get(1);
        assertThat(varScalarTwo.getOriginalText()).isEqualTo(finalText);
        assertThat(varScalarTwo.getStartPosition()).isEqualTo(
                (text + scalarOne).length());
        assertThat(varScalarTwo.getEndPosition()).isEqualTo(
                varScalarTwo.getStartPosition() + 2);

    }


    @Test
    public void getVariablesStartPosition_environmentVariableWithPrefixInBegin_shouldReturn_oneElementList() {
        String text = "%{environment}";
        assertOneVariablesOnlyInTextAfterPrefix("prefix ", text);
    }


    @Test
    public void getVariablesStartPosition_dictionaryVariableWithPrefixInBegin_shouldReturn_oneElementList() {
        String text = "&{dictionary}";
        assertOneVariablesOnlyInTextAfterPrefix("prefix ", text);
    }


    @Test
    public void getVariablesStartPosition_listVariableWithPrefixInBegin_shouldReturn_oneElementList() {
        String text = "@{list}";
        assertOneVariablesOnlyInTextAfterPrefix("prefix ", text);
    }


    @Test
    public void getVariablesStartPosition_scalarVariableWithPrefixInBegin_shouldReturn_oneElementList() {
        String text = "${scalar}";
        assertOneVariablesOnlyInTextAfterPrefix("prefix ", text);
    }


    @Test
    public void getVariablesStartPosition_environmentVariableOnlyInText_shouldReturn_oneElementList() {
        String text = "%{environment}";
        assertOneVariablesOnlyInTextAfterPrefix("", text);
    }


    @Test
    public void getVariablesStartPosition_dictionaryVariableOnlyInText_shouldReturn_oneElementList() {
        String text = "&{dictionary}";
        assertOneVariablesOnlyInTextAfterPrefix("", text);
    }


    @Test
    public void getVariablesStartPosition_listVariableOnlyInText_shouldReturn_oneElementList() {
        String text = "@{list}";
        assertOneVariablesOnlyInTextAfterPrefix("", text);
    }


    @Test
    public void getVariablesStartPosition_scalarVariableOnlyInText_shouldReturn_oneElementList() {
        String text = "${scalar}";
        assertOneVariablesOnlyInTextAfterPrefix("", text);
    }


    private void assertOneVariablesOnlyInTextAfterPrefix(String prefix,
            String text) {
        String joinedText = prefix + text;
        List<TextualPosition> variables = varExtractor
                .getVariablesStartPosition(joinedText);
        int prefixLength = prefix.length();

        assertThat(variables).hasSize(1);
        TextualPosition position = variables.get(0);
        assertThat(position.getOriginalText()).isEqualTo(joinedText);
        assertThat(position.getStartPosition()).isEqualTo(prefixLength);
        assertThat(position.getEndPosition()).isEqualTo(prefixLength + 2);
    }


    @Test
    public void getVariablesStartPosition_NULL_shouldReturn_emptyList() {
        assertThat(varExtractor.getVariablesStartPosition(null)).isEmpty();
    }


    @Before
    public void setUp() throws Exception {
        ClassFieldCleaner.init(this);
        varExtractor = new VariablesExtractor();
    }
}
