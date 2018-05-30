/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.mapping.hash.comment;

import java.util.Collections;
import java.util.List;

import org.rf.ide.core.testdata.mapping.IHashCommentMapper;
import org.rf.ide.core.testdata.mapping.table.ElementPositionResolver;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.table.RobotTokenPositionComparator;
import org.rf.ide.core.testdata.model.table.VariableTable;
import org.rf.ide.core.testdata.model.table.variables.AVariable;
import org.rf.ide.core.testdata.model.table.variables.AVariable.VariableScope;
import org.rf.ide.core.testdata.model.table.variables.IVariableHolder;
import org.rf.ide.core.testdata.model.table.variables.ListVariable;
import org.rf.ide.core.testdata.text.read.ParsingState;
import org.rf.ide.core.testdata.text.read.ParsingState.TableType;
import org.rf.ide.core.testdata.text.read.RobotLine;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

import com.google.common.annotations.VisibleForTesting;

public class VariablesDeclarationCommentMapper implements IHashCommentMapper {

    private final ElementPositionResolver resolver = new ElementPositionResolver();

    @Override
    public boolean isApplicable(final ParsingState state) {
        return (state == ParsingState.VARIABLE_UNKNOWN || state == ParsingState.VARIABLE_UNKNOWN_VALUE
                || state == ParsingState.SCALAR_VARIABLE_DECLARATION || state == ParsingState.SCALAR_VARIABLE_VALUE
                || state == ParsingState.LIST_VARIABLE_DECLARATION || state == ParsingState.LIST_VARIABLE_VALUE
                || state == ParsingState.DICTIONARY_VARIABLE_DECLARATION
                || state == ParsingState.DICTIONARY_VARIABLE_VALUE || state == ParsingState.VARIABLE_TABLE_INSIDE);
    }

    @Override
    public void map(final RobotLine currentLine, final RobotToken rt, final ParsingState currentState,
            final RobotFile fileModel) {
        final VariableTable variableTable = fileModel.getVariableTable();
        if (variableTable.isEmpty()) {
            final ListVariable var = new ListVariable(null, createArtifactalListVariable(rt), VariableScope.TEST_SUITE);
            var.addCommentPart(rt);
            variableTable.addVariable(var);
        } else {
            final List<AVariable> variables = variableTable.getVariables();
            final IVariableHolder var = variables.get(variables.size() - 1);
            if (isInTheSameLine(rt, var) || resolver.buildPositionDescription(fileModel, currentLine, rt)
                    .isContinuePreviousLineTheFirstToken(TableType.VARIABLES)) {
                var.addCommentPart(rt);
            } else {
                final ListVariable newVar = new ListVariable(null, createArtifactalListVariable(rt),
                        VariableScope.TEST_SUITE);
                newVar.addCommentPart(rt);
                variableTable.addVariable(newVar);
            }
        }
    }

    @VisibleForTesting
    protected boolean isInTheSameLine(final RobotToken rt, final IVariableHolder var) {
        boolean result = false;

        if (var instanceof AVariable) {
            final AVariable aVar = (AVariable) var;
            final List<RobotToken> tokens = aVar.getElementTokens();
            Collections.sort(tokens, new RobotTokenPositionComparator());
            final int size = tokens.size();
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
        final RobotToken token = new RobotToken();
        token.setLineNumber(rt.getLineNumber());
        token.setStartColumn(rt.getStartColumn());
        token.setText("");
        token.setType(RobotTokenType.VARIABLES_LIST_DECLARATION);

        return token;
    }
}
