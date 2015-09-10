/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.core.testData.model.table.variables.mapping;

import java.util.Stack;

import org.robotframework.ide.core.testData.model.FilePosition;
import org.robotframework.ide.core.testData.model.RobotFileOutput;
import org.robotframework.ide.core.testData.model.table.VariableTable;
import org.robotframework.ide.core.testData.model.table.mapping.ElementsUtility;
import org.robotframework.ide.core.testData.model.table.mapping.IParsingMapper;
import org.robotframework.ide.core.testData.model.table.variables.AVariable.VariableScope;
import org.robotframework.ide.core.testData.model.table.variables.ListVariable;
import org.robotframework.ide.core.testData.text.read.ParsingState;
import org.robotframework.ide.core.testData.text.read.RobotLine;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotToken;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotTokenType;


public class ListVariableMapper implements IParsingMapper {

    private final ElementsUtility utility;
    private final CommonVariableHelper varHelper;


    public ListVariableMapper() {
        this.utility = new ElementsUtility();
        this.varHelper = new CommonVariableHelper();
    }


    @Override
    public RobotToken map(RobotLine currentLine,
            Stack<ParsingState> processingState,
            RobotFileOutput robotFileOutput, RobotToken rt, FilePosition fp,
            String text) {
        VariableTable varTable = robotFileOutput.getFileModel()
                .getVariableTable();
        rt.setText(new StringBuilder(text));
        rt.setType(RobotTokenType.VARIABLES_LIST_DECLARATION);

        ListVariable var = new ListVariable(
                varHelper.extractVariableName(text), rt,
                VariableScope.TEST_SUITE);
        varTable.addVariable(var);

        processingState.push(ParsingState.LIST_VARIABLE_DECLARATION);

        return rt;
    }


    @Override
    public boolean checkIfCanBeMapped(RobotFileOutput robotFileOutput,
            RobotLine currentLine, RobotToken rt, String text,
            Stack<ParsingState> processingState) {
        boolean result = false;
        if (rt.getTypes().contains(RobotTokenType.VARIABLES_LIST_DECLARATION)) {
            if (utility.isTheFirstColumn(currentLine, rt)) {
                if (varHelper.isIncludedInVariableTable(currentLine,
                        processingState)) {
                    if (varHelper.isCorrectVariable(text)) {
                        result = true;
                    } else {
                        // FIXME: error here or in validation
                    }
                } else {
                    // FIXME: it is in wrong place means no variable table
                    // declaration
                }
            } else {
                // FIXME: wrong place case.
            }
        }
        return result;
    }
}
