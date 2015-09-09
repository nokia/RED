/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see licence.txt file for details.
 */
package org.robotframework.ide.core.testData.model.table.variables.mapping;

import java.util.List;
import java.util.Stack;

import org.robotframework.ide.core.testData.model.FilePosition;
import org.robotframework.ide.core.testData.model.RobotFileOutput;
import org.robotframework.ide.core.testData.model.table.mapping.ElementsUtility;
import org.robotframework.ide.core.testData.model.table.mapping.IParsingMapper;
import org.robotframework.ide.core.testData.model.table.variables.IVariableHolder;
import org.robotframework.ide.core.testData.model.table.variables.UnknownVariable;
import org.robotframework.ide.core.testData.text.read.ParsingState;
import org.robotframework.ide.core.testData.text.read.RobotLine;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotToken;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotTokenType;


public class UnknownVariableValueMapper implements IParsingMapper {

    private final ElementsUtility utility;


    public UnknownVariableValueMapper() {
        this.utility = new ElementsUtility();
    }


    @Override
    public RobotToken map(RobotLine currentLine,
            Stack<ParsingState> processingState,
            RobotFileOutput robotFileOutput, RobotToken rt, FilePosition fp,
            String text) {
        rt.setText(new StringBuilder(text));
        rt.setType(RobotTokenType.VARIABLES_VARIABLE_VALUE);

        List<IVariableHolder> variables = robotFileOutput.getFileModel()
                .getVariableTable().getVariables();
        if (!variables.isEmpty()) {
            UnknownVariable var = (UnknownVariable) variables.get(variables
                    .size() - 1);
            var.addItem(rt);
        } else {
            // FIXME: internall error
        }

        processingState.push(ParsingState.VARIABLE_UNKNOWN_VALUE);
        return rt;
    }


    @Override
    public boolean checkIfCanBeMapped(RobotFileOutput robotFileOutput,
            RobotLine currentLine, RobotToken rt, String text,
            Stack<ParsingState> processingState) {
        ParsingState currentState = utility.getCurrentStatus(processingState);

        return (currentState == ParsingState.VARIABLE_UNKNOWN || currentState == ParsingState.VARIABLE_UNKNOWN_VALUE);
    }

}
