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
import org.rf.ide.core.testdata.model.FilePosition;
import org.rf.ide.core.testdata.model.RobotFileOutput;
import org.rf.ide.core.testdata.model.table.VariableTable;
import org.rf.ide.core.testdata.model.table.variables.AVariable.VariableScope;
import org.rf.ide.core.testdata.model.table.variables.DictionaryVariable;
import org.rf.ide.core.testdata.text.read.IRobotTokenType;
import org.rf.ide.core.testdata.text.read.ParsingState;
import org.rf.ide.core.testdata.text.read.RobotLine;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

public class DictionaryVariableMapper implements IParsingMapper {

    private final ElementPositionResolver positionResolver;

    private final CommonVariableHelper varHelper;

    public DictionaryVariableMapper() {
        this.positionResolver = new ElementPositionResolver();
        this.varHelper = new CommonVariableHelper();
    }

    @Override
    public RobotToken map(final RobotLine currentLine, final Stack<ParsingState> processingState,
            final RobotFileOutput robotFileOutput, final RobotToken rt, final FilePosition fp, final String text) {
        final VariableTable varTable = robotFileOutput.getFileModel().getVariableTable();
        rt.setText(text);
        rt.setType(RobotTokenType.VARIABLES_DICTIONARY_DECLARATION);

        final DictionaryVariable var = new DictionaryVariable(varHelper.extractVariableName(text), rt,
                VariableScope.TEST_SUITE);
        varTable.addVariable(var);

        processingState.push(ParsingState.DICTIONARY_VARIABLE_DECLARATION);

        return rt;
    }

    @Override
    public boolean checkIfCanBeMapped(final RobotFileOutput robotFileOutput, final RobotLine currentLine,
            final RobotToken rt, final String text, final Stack<ParsingState> processingState) {
        boolean result = false;

        final List<IRobotTokenType> types = rt.getTypes();
        if (types.size() == 1 && types.get(0) == RobotTokenType.VARIABLES_DICTIONARY_DECLARATION) {
            if (positionResolver.isCorrectPosition(PositionExpected.VARIABLE_DECLARATION_IN_VARIABLE_TABLE,
                    robotFileOutput.getFileModel(), currentLine, rt)) {
                if (varHelper.isIncludedInVariableTable(currentLine, processingState)) {
                    if (varHelper.matchesBracketsConditionsForCorrectVariable(text)) {
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
