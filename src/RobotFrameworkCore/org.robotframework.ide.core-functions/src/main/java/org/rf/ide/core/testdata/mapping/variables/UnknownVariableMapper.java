/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.mapping.variables;

import java.util.List;
import java.util.Stack;

import org.rf.ide.core.testdata.mapping.table.ElementPositionResolver;
import org.rf.ide.core.testdata.mapping.table.ElementPositionResolver.PositionExpected;
import org.rf.ide.core.testdata.mapping.table.IParsingMapper;
import org.rf.ide.core.testdata.mapping.table.ParsingStateHelper;
import org.rf.ide.core.testdata.model.FilePosition;
import org.rf.ide.core.testdata.model.RobotFileOutput;
import org.rf.ide.core.testdata.model.table.VariableTable;
import org.rf.ide.core.testdata.model.table.variables.AVariable.VariableScope;
import org.rf.ide.core.testdata.model.table.variables.UnknownVariable;
import org.rf.ide.core.testdata.text.read.IRobotTokenType;
import org.rf.ide.core.testdata.text.read.ParsingState;
import org.rf.ide.core.testdata.text.read.RobotLine;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

public class UnknownVariableMapper implements IParsingMapper {

    private final ElementPositionResolver positionResolver;

    private final ParsingStateHelper stateHelper;

    public UnknownVariableMapper() {
        this.positionResolver = new ElementPositionResolver();
        this.stateHelper = new ParsingStateHelper();
    }

    @Override
    public RobotToken map(final RobotLine currentLine, final Stack<ParsingState> processingState,
            final RobotFileOutput robotFileOutput, final RobotToken rt, final FilePosition fp, final String text) {
        final List<IRobotTokenType> types = rt.getTypes();
        types.remove(RobotTokenType.UNKNOWN);
        types.add(0, RobotTokenType.VARIABLES_UNKNOWN_DECLARATION);
        rt.setStartColumn(fp.getColumn());
        rt.setText(text);

        final VariableTable varTable = robotFileOutput.getFileModel().getVariableTable();
        final UnknownVariable varUnknown = new UnknownVariable(rt.getRaw().toString(), rt, VariableScope.TEST_SUITE);
        varTable.addVariable(varUnknown);

        processingState.push(ParsingState.VARIABLE_UNKNOWN);

        return rt;
    }

    @Override
    public boolean checkIfCanBeMapped(final RobotFileOutput robotFileOutput, final RobotLine currentLine,
            final RobotToken rt, final String text, final Stack<ParsingState> processingState) {
        boolean result = false;
        final ParsingState currentState = stateHelper.getCurrentStatus(processingState);

        if (currentState == ParsingState.VARIABLE_TABLE_INSIDE) {
            if (text != null) {
                result = positionResolver.isCorrectPosition(PositionExpected.VARIABLE_DECLARATION_IN_VARIABLE_TABLE,
                        robotFileOutput.getFileModel(), currentLine, rt);
            }
        }

        return result;
    }
}
