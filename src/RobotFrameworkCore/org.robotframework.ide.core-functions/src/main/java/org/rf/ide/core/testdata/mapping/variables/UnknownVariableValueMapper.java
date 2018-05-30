/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.mapping.variables;

import java.util.List;
import java.util.Stack;

import org.rf.ide.core.testdata.mapping.table.IParsingMapper;
import org.rf.ide.core.testdata.mapping.table.ParsingStateHelper;
import org.rf.ide.core.testdata.model.FilePosition;
import org.rf.ide.core.testdata.model.RobotFileOutput;
import org.rf.ide.core.testdata.model.table.variables.AVariable;
import org.rf.ide.core.testdata.model.table.variables.UnknownVariable;
import org.rf.ide.core.testdata.text.read.ParsingState;
import org.rf.ide.core.testdata.text.read.RobotLine;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

public class UnknownVariableValueMapper implements IParsingMapper {

    private final ParsingStateHelper utility;

    public UnknownVariableValueMapper() {
        this.utility = new ParsingStateHelper();
    }

    @Override
    public RobotToken map(final RobotLine currentLine,
            final Stack<ParsingState> processingState,
            final RobotFileOutput robotFileOutput, final RobotToken rt, final FilePosition fp,
            final String text) {
        rt.setText(text);
        rt.setType(RobotTokenType.VARIABLES_VARIABLE_VALUE);

        final List<AVariable> variables = robotFileOutput.getFileModel()
                .getVariableTable().getVariables();
        if (!variables.isEmpty()) {
            final UnknownVariable var = (UnknownVariable) variables.get(variables
                    .size() - 1);
            var.addItem(rt);
        } else {
            // FIXME: internal error
        }

        processingState.push(ParsingState.VARIABLE_UNKNOWN_VALUE);
        return rt;
    }

    @Override
    public boolean checkIfCanBeMapped(final RobotFileOutput robotFileOutput,
            final RobotLine currentLine, final RobotToken rt, final String text,
            final Stack<ParsingState> processingState) {
        final ParsingState currentState = utility.getCurrentStatus(processingState);

        return (currentState == ParsingState.VARIABLE_UNKNOWN || currentState == ParsingState.VARIABLE_UNKNOWN_VALUE);
    }

}
