/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.mapping.table;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.junit.Test;
import org.rf.ide.core.testdata.model.FilePosition;
import org.rf.ide.core.testdata.text.read.ParsingState;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

/**
 * @author lwlodarc
 *
 */
public class ElementsUtilityTest {

    private final ElementsUtility elementsUtility = new ElementsUtility();

    @Test
    public void robotTokenHasProperTypes_forKnownTableHeader() {
        // prepare
        final Stack<ParsingState> processingState = stack(ParsingState.TEST_CASE_TABLE_INSIDE);
        final FilePosition fp = new FilePosition(4, 0, 44);

        final String text = "*** Settings ***";

        final List<RobotToken> robotTokens = new ArrayList<>();
        robotTokens.add(RobotToken.create(text, new FilePosition(4, 0, -1), RobotTokenType.SETTINGS_TABLE_HEADER));

        // execute
        final RobotToken result = elementsUtility.computeCorrectRobotToken(processingState, fp, text, robotTokens);

        // verify
        assertThat(result.getText()).isEqualTo(text);
        assertThat(result.getFilePosition()).isEqualTo(new FilePosition(4, 0, -1));
        assertThat(result.getTypes()).containsExactly(RobotTokenType.SETTINGS_TABLE_HEADER);
    }

    @Test
    public void robotTokenHasProperTypes_forUnknownTableHeader() {
        // prepare
        final Stack<ParsingState> processingState = stack(ParsingState.TEST_CASE_TABLE_INSIDE);
        final FilePosition fp = new FilePosition(4, 0, 44);

        final String text = "*** Unknown ***";

        final List<RobotToken> robotTokens = new ArrayList<>();
        robotTokens.add(RobotToken.create(text, new FilePosition(4, 0, -1), RobotTokenType.UNKNOWN));

        // execute
        final RobotToken result = elementsUtility.computeCorrectRobotToken(processingState, fp, text, robotTokens);

        // verify
        assertThat(result.getText()).isEqualTo(text);
        assertThat(result.getFilePosition()).isEqualTo(new FilePosition(4, 0, -1));
        assertThat(result.getTypes()).containsExactly(RobotTokenType.UNKNOWN);
    }

    @Test
    public void robotTokenHasProperTypes_forUnknownCombinedTableHeader() {
        // prepare
        final Stack<ParsingState> processingState = stack(ParsingState.TEST_CASE_TABLE_INSIDE,
                ParsingState.TEST_CASE_DECLARATION);
        final FilePosition fp = new FilePosition(4, 0, 44);

        final String text = "** * Settings ************* Variables";

        final List<RobotToken> robotTokens = new ArrayList<>();
        robotTokens.add(RobotToken.create("** * Settings *************", new FilePosition(4, 0, -1),
                RobotTokenType.SETTINGS_TABLE_HEADER));
        robotTokens.add(RobotToken.create(" ************* Variables", new FilePosition(4, 13, -1),
                RobotTokenType.VARIABLES_TABLE_HEADER));
        robotTokens.add(RobotToken.create(" Variables", new FilePosition(4, 27, -1),
                RobotTokenType.SETTING_VARIABLES_DECLARATION));

        // execute
        final RobotToken result = elementsUtility.computeCorrectRobotToken(processingState, fp, text, robotTokens);

        // verify
        assertThat(result.getText()).isEqualTo(text);
        assertThat(result.getFilePosition()).isEqualTo(new FilePosition(4, 0, -1));
        assertThat(result.getTypes()).containsExactly(RobotTokenType.UNKNOWN);
    }

    @Test
    public void robotTokenHasProperTypes_forNestedDictionaryVariable() {
        testNestedVariableOfTypeAndSymbol(RobotTokenType.VARIABLES_DICTIONARY_DECLARATION, "&");
    }

    @Test
    public void robotTokenHasProperTypes_forNestedListVariable() {
        testNestedVariableOfTypeAndSymbol(RobotTokenType.VARIABLES_LIST_DECLARATION, "@");
    }

    @Test
    public void robotTokenHasProperTypes_forNestedScalarVariable() {
        testNestedVariableOfTypeAndSymbol(RobotTokenType.VARIABLES_SCALAR_DECLARATION, "$");
    }

    private void testNestedVariableOfTypeAndSymbol(final RobotTokenType type, final String symbol) {
        // prepare
        final Stack<ParsingState> processingState = stack(ParsingState.KEYWORD_TABLE_INSIDE,
                ParsingState.KEYWORD_DECLARATION, ParsingState.KEYWORD_INSIDE_ACTION);
        final FilePosition fp = new FilePosition(10, 10, -1);
        final FilePosition fpInside = new FilePosition(10, 12, -1);

        final String text = symbol + "{${var_name}}";

        final List<RobotToken> robotTokens = new ArrayList<>();
        robotTokens.add(RobotToken.create("${var_name}}", fpInside, RobotTokenType.VARIABLES_SCALAR_DECLARATION));
        robotTokens.add(RobotToken.create(text, fp, type));

        // execute
        final RobotToken result = elementsUtility.computeCorrectRobotToken(processingState, fp, text, robotTokens);

        // verify
        assertThat(result.getText()).isEqualTo(text);
        assertThat(result.getFilePosition()).isEqualTo(fp);
        assertThat(result.getTypes()).containsExactly(type, RobotTokenType.VARIABLE_USAGE);
    }

    private static Stack<ParsingState> stack(final ParsingState... states) {
        final Stack<ParsingState> stack = new Stack<>();
        for (final ParsingState state : states) {
            stack.push(state);
        }
        return stack;
    }
}
