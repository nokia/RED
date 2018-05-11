/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.table.exec.descs.impl;

import java.util.List;

import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.RobotFileOutput;
import org.rf.ide.core.testdata.model.table.ARobotSectionTable;
import org.rf.ide.core.testdata.model.table.RobotExecutableRow;
import org.rf.ide.core.testdata.model.table.exec.CommentedVariablesFilter;
import org.rf.ide.core.testdata.model.table.exec.CommentedVariablesFilter.FilteredVariables;
import org.rf.ide.core.testdata.model.table.exec.descs.IExecutableRowDescriptor;
import org.rf.ide.core.testdata.model.table.exec.descs.IRowDescriptorBuilder;
import org.rf.ide.core.testdata.model.table.exec.descs.RobotAction;
import org.rf.ide.core.testdata.model.table.exec.descs.VariableExtractor;
import org.rf.ide.core.testdata.model.table.exec.descs.ast.mapping.IElementDeclaration;
import org.rf.ide.core.testdata.model.table.exec.descs.ast.mapping.JoinedTextDeclarations;
import org.rf.ide.core.testdata.model.table.exec.descs.ast.mapping.MappingResult;
import org.rf.ide.core.testdata.model.table.exec.descs.ast.mapping.VariableDeclaration;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

public class SimpleRowDescriptorBuilder implements IRowDescriptorBuilder {

    @Override
    public <T> AcceptResult acceptable(final RobotExecutableRow<T> execRowLine) {
        return new AcceptResult(true);
    }

    @Override
    public <T> IExecutableRowDescriptor<T> buildDescription(final RobotExecutableRow<T> execRowLine,
            final AcceptResult acceptResult) {
        final SimpleRowDescriptor<T> simple = new SimpleRowDescriptor<>(execRowLine);
        final ARobotSectionTable table;
        if (execRowLine.getParent() instanceof AModelElement) {
            final AModelElement<?> keywordOrTestcase = (AModelElement<?>) execRowLine.getParent();
            table = (ARobotSectionTable) keywordOrTestcase.getParent();
        } else {
            table = (ARobotSectionTable) execRowLine.getParent();
        }

        final RobotFile robotFile = table.getParent();
        final RobotFileOutput rfo = robotFile.getParent();
        final String fileName = rfo.getProcessedFile().getAbsolutePath();

        final VariableExtractor varExtractor = new VariableExtractor();
        final List<RobotToken> lineElements = execRowLine.getElementTokens();
        boolean isAfterFirstAction = false;
        final CommentedVariablesFilter filter = new CommentedVariablesFilter();
        for (final RobotToken elem : lineElements) {
            final MappingResult mappingResult = varExtractor.extract(elem, fileName);
            simple.addMessages(mappingResult.getMessages());

            // value is a keyword if is on the first place and is not just a variable or
            // variable with equal sign. Keyword can be defined as a ${var}=, however must
            // be called with a plain text in the place of an embedded variable
            final FilteredVariables filteredVars = filter.filter(rfo, mappingResult.getCorrectVariables());
            simple.addCommentedVariables(filteredVars.getCommented());
            final List<VariableDeclaration> correctVariables = filteredVars.getUsed();
            final List<IElementDeclaration> mappedElements = mappingResult.getMappedElements();
            if (isAfterFirstAction) {
                simple.addUsedVariables(correctVariables);
                simple.addTextParameters(mappingResult.getTextElements());
                if (!execRowLine.getComment().contains(elem)) {
                    simple.addKeywordArgument(elem.copy());
                }
            } else {
                if (correctVariables.size() == 1 && (mappedElements.size() == 1
                        || (mappedElements.size() == 2
                                && mappedElements.get(1) instanceof JoinedTextDeclarations
                                && "=".equals(((JoinedTextDeclarations) (mappedElements.get(1))).getText().trim())))) {
                    // definition variable
                    simple.addCreatedVariable(correctVariables.get(0));
                } else {
                    if (elem.getTypes().contains(RobotTokenType.START_HASH_COMMENT)) {
                        simple.addTextParameters(mappingResult.getTextElements());
                    } else {
                        simple.setAction(new RobotAction(elem.copy(), mappedElements));
                        simple.addUsedVariables(correctVariables);
                        isAfterFirstAction = true;
                    }
                }
            }
        }

        moveCreatedToUsedInCaseNoKeywords(simple);
        return simple;
    }

    private <T> void moveCreatedToUsedInCaseNoKeywords(final SimpleRowDescriptor<T> simple) {
        if (simple.getAction().getToken().getFilePosition().isNotSet() && !simple.getAction().getToken().isNotEmpty()
                && simple.getKeywordArguments().isEmpty()) {
            simple.moveCreatedVariablesToUsedVariables();
        }
    }
}
