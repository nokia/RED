/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.table.exec.descs.impl;

import java.util.List;

import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.FilePosition;
import org.rf.ide.core.testdata.model.FileRegion;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.RobotFileOutput;
import org.rf.ide.core.testdata.model.RobotFileOutput.BuildMessage;
import org.rf.ide.core.testdata.model.table.ARobotSectionTable;
import org.rf.ide.core.testdata.model.table.RobotExecutableRow;
import org.rf.ide.core.testdata.model.table.exec.CommentedVariablesFilter;
import org.rf.ide.core.testdata.model.table.exec.CommentedVariablesFilter.FilteredVariables;
import org.rf.ide.core.testdata.model.table.exec.descs.ForDescriptorInfo;
import org.rf.ide.core.testdata.model.table.exec.descs.IExecutableRowDescriptor;
import org.rf.ide.core.testdata.model.table.exec.descs.IRowDescriptorBuilder;
import org.rf.ide.core.testdata.model.table.exec.descs.RobotAction;
import org.rf.ide.core.testdata.model.table.exec.descs.VariableExtractor;
import org.rf.ide.core.testdata.model.table.exec.descs.ast.mapping.IElementDeclaration;
import org.rf.ide.core.testdata.model.table.exec.descs.ast.mapping.MappingResult;
import org.rf.ide.core.testdata.model.table.exec.descs.ast.mapping.VariableDeclaration;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;

public class ForLoopDeclarationRowDescriptorBuilder implements IRowDescriptorBuilder {

    @Override
    public <T> AcceptResult acceptable(final RobotExecutableRow<T> execRowLine) {
        return new AcceptResult(ForDescriptorInfo.isForToken(execRowLine.getAction()));
    }

    @Override
    public <T> IExecutableRowDescriptor<T> buildDescription(final RobotExecutableRow<T> execRowLine,
            final AcceptResult acceptResult) {
        final ForLoopDeclarationRowDescriptor<T> loopDescriptor = new ForLoopDeclarationRowDescriptor<>(execRowLine);

        final AModelElement<?> keywordOrTestcase = (AModelElement<?>) execRowLine.getParent();
        final ARobotSectionTable table = (ARobotSectionTable) keywordOrTestcase.getParent();
        final RobotFile robotFile = table.getParent();
        final RobotFileOutput rfo = robotFile.getParent();
        final String fileName = rfo.getProcessedFile().getAbsolutePath();

        final VariableExtractor varExtractor = new VariableExtractor();
        final List<RobotToken> lineElements = execRowLine.getElementTokens();
        boolean wasFor = false;
        boolean wasIn = false;
        boolean wasElementsToIterate = false;
        final CommentedVariablesFilter filter = new CommentedVariablesFilter();
        for (final RobotToken elem : lineElements) {
            final MappingResult mappingResult = varExtractor.extract(elem, fileName);
            loopDescriptor.addMessages(mappingResult.getMessages());

            // value is keyword if is on the first place and have in it nested
            // variables and when contains text on the beginning or end of field
            FilteredVariables filteredVars = filter.filter(rfo, mappingResult.getCorrectVariables());
            loopDescriptor.addCommentedVariables(filteredVars.getCommented());
            final List<VariableDeclaration> correctVariables = filteredVars.getUsed();
            final List<IElementDeclaration> mappedElements = mappingResult.getMappedElements();

            if (wasFor) {
                if (wasIn) {
                    loopDescriptor.addUsedVariables(correctVariables);
                    loopDescriptor.addTextParameters(mappingResult.getTextElements());
                    wasElementsToIterate = true;
                } else {
                    if (ForDescriptorInfo.isInToken(elem)) {
                        loopDescriptor.setInAction(new RobotAction(elem, mappedElements));
                        wasIn = true;
                    } else {
                        final int variablesSize = correctVariables.size();
                        loopDescriptor.addCreatedVariables(correctVariables);

                        if (!mappingResult.getTextElements().isEmpty() || variablesSize > 1) {
                            final BuildMessage errorMessage = BuildMessage.createErrorMessage(
                                    "Invalid FOR loop variable \'" + elem.getText().toString() + "\'", fileName);
                            final FilePosition startFilePosition = elem.getFilePosition();
                            final FilePosition end = new FilePosition(startFilePosition.getLine(), elem.getEndColumn(),
                                    elem.getStartOffset() + elem.getText().length());
                            errorMessage.setFileRegion(new FileRegion(startFilePosition, end));
                            loopDescriptor.addMessage(errorMessage);
                        }
                    }
                }
            } else {
                if (ForDescriptorInfo.isForToken(elem)) {
                    loopDescriptor.setAction(new RobotAction(elem, mappedElements));
                    wasFor = true;
                } else {
                    throw new IllegalStateException("Internal problem - :FOR should be the first token.");
                }
            }
        }

        if (!wasIn || !wasElementsToIterate) {
            final BuildMessage errorMessage = BuildMessage
                    .createErrorMessage("Invalid FOR loop - missing values to iterate", fileName);
            final FilePosition startFilePosition = keywordOrTestcase.getBeginPosition();
            errorMessage.setFileRegion(new FileRegion(startFilePosition, execRowLine.getEndPosition()));
            loopDescriptor.addMessage(errorMessage);
        }

        return loopDescriptor;
    }
}
