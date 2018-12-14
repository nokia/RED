/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.table.exec.descs.impl;

import java.util.List;

import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.RobotFileOutput;
import org.rf.ide.core.testdata.model.table.ARobotSectionTable;
import org.rf.ide.core.testdata.model.table.RobotExecutableRow;
import org.rf.ide.core.testdata.model.table.exec.descs.IExecutableRowDescriptor;
import org.rf.ide.core.testdata.model.table.exec.descs.IRowDescriptorBuilder;
import org.rf.ide.core.testdata.model.table.exec.descs.RobotAction;
import org.rf.ide.core.testdata.model.table.exec.descs.VariableExtractor;
import org.rf.ide.core.testdata.model.table.exec.descs.ast.mapping.IElementDeclaration;
import org.rf.ide.core.testdata.model.table.exec.descs.ast.mapping.JoinedTextDeclarations;
import org.rf.ide.core.testdata.model.table.exec.descs.ast.mapping.MappingResult;
import org.rf.ide.core.testdata.model.table.exec.descs.ast.mapping.VariableDeclaration;
import org.rf.ide.core.testdata.model.table.exec.descs.impl.CommentedVariablesFilter.FilteredVariables;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

public class SimpleRowDescriptorBuilder implements IRowDescriptorBuilder {

    @Override
    public <T> boolean isAcceptable(final RobotExecutableRow<T> execRowLine) {
        return true;
    }

    @Override
    public <T> IExecutableRowDescriptor<T> buildDescription(final RobotExecutableRow<T> execRowLine) {
        final SimpleRowDescriptor<T> simpleDesc = new SimpleRowDescriptor<>(execRowLine);

        final RobotFileOutput rfo = getFileOutput(execRowLine);
        final String fileName = rfo.getProcessedFile().getAbsolutePath();

        final VariableExtractor varExtractor = new VariableExtractor();
        final List<RobotToken> lineElements = execRowLine.getElementTokens();
        boolean isAfterFirstAction = false;
        final CommentedVariablesFilter filter = new CommentedVariablesFilter();
        for (final RobotToken elem : lineElements) {
            final MappingResult mappingResult = varExtractor.extract(elem, fileName);
            simpleDesc.addMessages(mappingResult.getMessages());

            // value is a keyword if is on the first place and is not just a variable or
            // variable with equal sign. Keyword can be defined as a ${var}=, however must
            // be called with a plain text in the place of an embedded variable
            final FilteredVariables filteredVars = filter.filter(rfo, mappingResult.getCorrectVariables());
            simpleDesc.addCommentedVariables(filteredVars.getCommented());
            final List<VariableDeclaration> correctVariables = filteredVars.getUsed();
            final List<IElementDeclaration> mappedElements = mappingResult.getMappedElements();
            if (isAfterFirstAction) {
                simpleDesc.addUsedVariables(correctVariables);
                simpleDesc.addTextParameters(mappingResult.getTextElements());
                if (!execRowLine.getComment().contains(elem)) {
                    simpleDesc.addKeywordArgument(elem.copy());
                }
            } else {
                if (correctVariables.size() == 1 && isVariableDefinition(mappedElements)) {
                    simpleDesc.addCreatedVariable(correctVariables.get(0));
                } else {
                    if (elem.getTypes().contains(RobotTokenType.START_HASH_COMMENT)) {
                        simpleDesc.addTextParameters(mappingResult.getTextElements());
                    } else {
                        simpleDesc.setAction(new RobotAction(elem.copy(), mappedElements));
                        simpleDesc.addUsedVariables(correctVariables);
                        isAfterFirstAction = true;
                    }
                }
            }
        }

        moveCreatedToUsedInCaseNoKeywords(simpleDesc);
        return simpleDesc;
    }

    private boolean isVariableDefinition(final List<IElementDeclaration> mappedElements) {
        if (mappedElements.size() == 1) {
            return true;
        } else if (mappedElements.size() == 2) {
            final IElementDeclaration lastElement = mappedElements.get(1);
            return lastElement instanceof JoinedTextDeclarations
                    && "=".equals(((JoinedTextDeclarations) lastElement).getText().trim());
        }
        return false;
    }

    private <T> void moveCreatedToUsedInCaseNoKeywords(final SimpleRowDescriptor<T> simple) {
        if (simple.getAction().getToken().getFilePosition().isNotSet() && !simple.getAction().getToken().isNotEmpty()
                && simple.getKeywordArguments().isEmpty()) {
            simple.moveCreatedVariablesToUsedVariables();
        }
    }

    private <T> RobotFileOutput getFileOutput(final RobotExecutableRow<T> execRowLine) {
        final ARobotSectionTable table;
        if (execRowLine.getParent() instanceof AModelElement) {
            final AModelElement<?> execParent = (AModelElement<?>) execRowLine.getParent();
            table = (ARobotSectionTable) execParent.getParent();
        } else {
            table = (ARobotSectionTable) execRowLine.getParent();
        }
        return table.getParent().getParent();
    }
}
