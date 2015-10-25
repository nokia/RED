/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.core.testData.model.table.variables.mapping;

import java.util.List;
import java.util.Stack;

import org.robotframework.ide.core.testData.model.FilePosition;
import org.robotframework.ide.core.testData.model.RobotFileOutput;
import org.robotframework.ide.core.testData.model.table.VariableTable;
import org.robotframework.ide.core.testData.model.table.mapping.ElementPositionResolver;
import org.robotframework.ide.core.testData.model.table.mapping.ElementPositionResolver.PositionExpected;
import org.robotframework.ide.core.testData.model.table.mapping.IParsingMapper;
import org.robotframework.ide.core.testData.model.table.mapping.ParsingStateHelper;
import org.robotframework.ide.core.testData.model.table.variables.AVariable.VariableScope;
import org.robotframework.ide.core.testData.model.table.variables.UnknownVariable;
import org.robotframework.ide.core.testData.text.read.IRobotTokenType;
import org.robotframework.ide.core.testData.text.read.ParsingState;
import org.robotframework.ide.core.testData.text.read.RobotLine;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotToken;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotTokenType;


public class UnknownVariableMapper implements IParsingMapper {

    private final ElementPositionResolver positionResolver;
    private final ParsingStateHelper stateHelper;


    public UnknownVariableMapper() {
        this.positionResolver = new ElementPositionResolver();
        this.stateHelper = new ParsingStateHelper();
    }


    @Override
    public RobotToken map(RobotLine currentLine,
            Stack<ParsingState> processingState,
            RobotFileOutput robotFileOutput, RobotToken rt, FilePosition fp,
            String text) {
        List<IRobotTokenType> types = rt.getTypes();
        types.remove(RobotTokenType.UNKNOWN);
        types.add(0, RobotTokenType.VARIABLES_UNKNOWN_DECLARATION);
        rt.setStartColumn(fp.getColumn());
        rt.setText(new StringBuilder(text));
        rt.setRaw(new StringBuilder(text));

        VariableTable varTable = robotFileOutput.getFileModel()
                .getVariableTable();
        UnknownVariable varUnknown = new UnknownVariable(
                rt.getRaw().toString(), rt, VariableScope.TEST_SUITE);
        varTable.addVariable(varUnknown);

        processingState.push(ParsingState.VARIABLE_UNKNOWN);

        return rt;
    }


    @Override
    public boolean checkIfCanBeMapped(RobotFileOutput robotFileOutput,
            RobotLine currentLine, RobotToken rt, String text,
            Stack<ParsingState> processingState) {
        boolean result = false;
        ParsingState currentState = stateHelper
                .getCurrentStatus(processingState);

        if (currentState == ParsingState.VARIABLE_TABLE_INSIDE) {
            if (text != null) {
                result = positionResolver
                        .isCorrectPosition(
                                PositionExpected.VARIABLE_DECLARATION_IN_VARIABLE_TABLE,
                                robotFileOutput.getFileModel(), currentLine, rt);
            }
        }

        return result;
    }
}
