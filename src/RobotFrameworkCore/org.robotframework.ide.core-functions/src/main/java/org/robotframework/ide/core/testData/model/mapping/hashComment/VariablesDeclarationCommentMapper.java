/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.core.testData.model.mapping.hashComment;

import java.util.Collections;
import java.util.List;

import org.robotframework.ide.core.testData.model.RobotFile;
import org.robotframework.ide.core.testData.model.mapping.IHashCommentMapper;
import org.robotframework.ide.core.testData.model.table.RobotTokenPositionComparator;
import org.robotframework.ide.core.testData.model.table.VariableTable;
import org.robotframework.ide.core.testData.model.table.variables.AVariable;
import org.robotframework.ide.core.testData.model.table.variables.IVariableHolder;
import org.robotframework.ide.core.testData.model.table.variables.ListVariable;
import org.robotframework.ide.core.testData.text.read.ParsingState;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotToken;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotTokenType;

import com.google.common.annotations.VisibleForTesting;


public class VariablesDeclarationCommentMapper implements IHashCommentMapper {

    @Override
    public boolean isApplicable(ParsingState state) {
        return (state == ParsingState.SCALAR_VARIABLE_DECLARATION
                || state == ParsingState.SCALAR_VARIABLE_VALUE
                || state == ParsingState.LIST_VARIABLE_DECLARATION
                || state == ParsingState.LIST_VARIABLE_VALUE
                || state == ParsingState.DICTIONARY_VARIABLE_DECLARATION
                || state == ParsingState.DICTIONARY_VARIABLE_VALUE || state == ParsingState.VARIABLE_TABLE_INSIDE);
    }


    @Override
    public void map(RobotToken rt, ParsingState currentState,
            RobotFile fileModel) {
        VariableTable variableTable = fileModel.getVariableTable();
        if (variableTable.isEmpty()) {
            ListVariable var = new ListVariable(null,
                    createArtifactalListVariable(rt));
            var.addCommentPart(rt);
            variableTable.addVariable(var);
        } else {
            List<IVariableHolder> variables = variableTable.getVariables();
            IVariableHolder var = variables.get(variables.size() - 1);
            if (isInTheSameLine(rt, var)) {
                var.addCommentPart(rt);
            } else {
                ListVariable newVar = new ListVariable(null,
                        createArtifactalListVariable(rt));
                newVar.addCommentPart(rt);
                variableTable.addVariable(newVar);
            }
        }
    }


    @VisibleForTesting
    protected boolean isInTheSameLine(final RobotToken rt,
            final IVariableHolder var) {
        boolean result = false;

        if (var instanceof AVariable) {
            AVariable aVar = (AVariable) var;
            List<RobotToken> tokens = aVar.getElementTokens();
            Collections.sort(tokens, new RobotTokenPositionComparator());
            int size = tokens.size();
            for (int i = size - 1; i >= 0; i--) {
                if (tokens.get(i).getLineNumber() == rt.getLineNumber()) {
                    result = true;
                    break;
                }
            }
        }

        return result;
    }


    @VisibleForTesting
    protected RobotToken createArtifactalListVariable(final RobotToken rt) {
        RobotToken token = new RobotToken();
        token.setLineNumber(rt.getLineNumber());
        token.setStartColumn(rt.getStartColumn());
        token.setText(new StringBuilder());
        token.setRaw(new StringBuilder());
        token.setType(RobotTokenType.VARIABLES_LIST_DECLARATION);

        return token;
    }
}
