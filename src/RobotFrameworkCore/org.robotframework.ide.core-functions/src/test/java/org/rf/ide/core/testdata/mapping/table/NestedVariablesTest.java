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
import org.rf.ide.core.testdata.model.RobotFileOutput;
import org.rf.ide.core.testdata.model.RobotVersion;
import org.rf.ide.core.testdata.text.read.IRobotTokenType;
import org.rf.ide.core.testdata.text.read.ParsingState;
import org.rf.ide.core.testdata.text.read.RobotLine;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

/**
 * @author lwlodarc
 *
 */
public class NestedVariablesTest {

    private final ElementsUtility elementsUtility = new ElementsUtility();

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
        final RobotLine line = new RobotLine(1, null);
        final Stack<ParsingState> processingState = new Stack<>();
        processingState.push(ParsingState.KEYWORD_TABLE_INSIDE);
        processingState.push(ParsingState.KEYWORD_DECLARATION);
        processingState.push(ParsingState.KEYWORD_INSIDE_ACTION);
        final RobotFileOutput rfo = new RobotFileOutput(new RobotVersion(3, 0));
        final FilePosition fp = new FilePosition(10, 10, -1);
        final FilePosition fpInside = new FilePosition(10, 12, -1);

        final String text = symbol + "{${var_name}}";

        final List<RobotToken> robotTokens = new ArrayList<>();
        final List<IRobotTokenType> scalarType = new ArrayList<>();
        scalarType.add(RobotTokenType.VARIABLES_SCALAR_DECLARATION);
        robotTokens.add(RobotToken.create("${var_name}}", scalarType));
        final List<IRobotTokenType> dictionaryType = new ArrayList<>();
        dictionaryType.add(type);
        robotTokens.add(RobotToken.create(text, dictionaryType));
        robotTokens.get(0).setFilePosition(fpInside);
        robotTokens.get(1).setFilePosition(fp);
        final List<IRobotTokenType> wantedResults = new ArrayList<>();
        wantedResults.add(type);
        wantedResults.add(RobotTokenType.VARIABLE_USAGE);
        final String fileName = "fake.robot";

        // execute
        final RobotToken result = elementsUtility.computeCorrectRobotToken(line, processingState, rfo, fp, text, false,
                robotTokens, fileName);

        // verify
        assertThat(result.getText()).isEqualTo(text);
        assertThat(result.getFilePosition()).isEqualTo(fp);
        assertThat(result.getTypes()).hasSameElementsAs(wantedResults);
    }
}
