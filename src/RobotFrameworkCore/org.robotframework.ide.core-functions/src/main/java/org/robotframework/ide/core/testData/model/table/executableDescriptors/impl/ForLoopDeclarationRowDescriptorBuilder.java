/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.core.testData.model.table.executableDescriptors.impl;

import java.util.List;

import org.robotframework.ide.core.testData.model.AModelElement;
import org.robotframework.ide.core.testData.model.FilePosition;
import org.robotframework.ide.core.testData.model.FileRegion;
import org.robotframework.ide.core.testData.model.RobotFile;
import org.robotframework.ide.core.testData.model.RobotFileOutput.BuildMessage;
import org.robotframework.ide.core.testData.model.table.ARobotSectionTable;
import org.robotframework.ide.core.testData.model.table.RobotExecutableRow;
import org.robotframework.ide.core.testData.model.table.executableDescriptors.ForDescriptorInfo;
import org.robotframework.ide.core.testData.model.table.executableDescriptors.IExecutableRowDescriptor;
import org.robotframework.ide.core.testData.model.table.executableDescriptors.IRowDescriptorBuilder;
import org.robotframework.ide.core.testData.model.table.executableDescriptors.RobotAction;
import org.robotframework.ide.core.testData.model.table.executableDescriptors.VariableExtractor;
import org.robotframework.ide.core.testData.model.table.executableDescriptors.ast.mapping.IElementDeclaration;
import org.robotframework.ide.core.testData.model.table.executableDescriptors.ast.mapping.MappingResult;
import org.robotframework.ide.core.testData.model.table.executableDescriptors.ast.mapping.VariableDeclaration;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotToken;


public class ForLoopDeclarationRowDescriptorBuilder implements
        IRowDescriptorBuilder {

    @Override
    public <T> AcceptResult acceptable(final RobotExecutableRow<T> execRowLine) {
        return new AcceptResult(ForDescriptorInfo.isForToken(execRowLine
                .getAction()));
    }


    @Override
    public <T> IExecutableRowDescriptor<T> buildDescription(
            final RobotExecutableRow<T> execRowLine,
            final AcceptResult acceptResult) {
        final ForLoopDeclarationRowDescriptor<T> loopDescriptor = new ForLoopDeclarationRowDescriptor<>(
                execRowLine);

        final AModelElement<?> keywordOrTestcase = (AModelElement<?>) execRowLine
                .getParent();
        final ARobotSectionTable table = (ARobotSectionTable) keywordOrTestcase
                .getParent();
        final RobotFile robotFile = table.getParent();
        final String fileName = robotFile.getParent().getProcessedFile()
                .getAbsolutePath();

        final VariableExtractor varExtractor = new VariableExtractor();
        final List<RobotToken> lineElements = execRowLine.getElementTokens();
        boolean wasFor = false;
        boolean wasIn = false;
        boolean wasElementsToIterate = false;
        for (final RobotToken elem : lineElements) {
            final MappingResult mappingResult = varExtractor.extract(elem, fileName);
            loopDescriptor.addMessages(mappingResult.getMessages());

            // value is keyword if is on the first place and have in it nested
            // variables and when contains text on the beginning or end of field
            final List<VariableDeclaration> correctVariables = mappingResult
                    .getCorrectVariables();
            final List<IElementDeclaration> mappedElements = mappingResult
                    .getMappedElements();

            if (wasFor) {
                if (wasIn) {
                    loopDescriptor.addUsedVariables(correctVariables);
                    loopDescriptor.addTextParameters(mappingResult
                            .getTextElements());
                    wasElementsToIterate = true;
                } else {
                    if (ForDescriptorInfo.isInToken(elem)) {
                        loopDescriptor.setInAction(new RobotAction(elem,
                                mappedElements));
                        wasIn = true;
                    } else {
                        final int variablesSize = correctVariables.size();
                        loopDescriptor.addCreatedVariables(correctVariables);

                        if (!mappingResult.getTextElements().isEmpty()
                                || variablesSize > 1) {
                            final BuildMessage errorMessage = BuildMessage
                                    .createErrorMessage(
                                            "Invalid FOR loop variable \'"
                                                    + elem.getText().toString()
                                                    + "\'", fileName);
                            final FilePosition startFilePosition = elem
                                    .getFilePosition();
                            final FilePosition end = new FilePosition(
                                    startFilePosition.getLine(),
                                    elem.getEndColumn(), elem.getStartOffset()
                                            + elem.getText().length());
                            errorMessage.setFileRegion(new FileRegion(
                                    startFilePosition, end));
                            loopDescriptor.addMessage(errorMessage);
                        }
                    }
                }
            } else {
                if (ForDescriptorInfo.isForToken(elem)) {
                    loopDescriptor.setAction(new RobotAction(elem,
                            mappedElements));
                    wasFor = true;
                } else {
                    throw new IllegalStateException(
                            "Internal problem - :FOR should be the first token.");
                }
            }
        }

        if (!wasIn || !wasElementsToIterate) {
            final BuildMessage errorMessage = BuildMessage.createErrorMessage(
                    "Invalid FOR loop - missing values to iterate", fileName);
            final FilePosition startFilePosition = keywordOrTestcase
                    .getBeginPosition();
            errorMessage.setFileRegion(new FileRegion(startFilePosition,
                    execRowLine.getEndPosition()));
            loopDescriptor.addMessage(errorMessage);
        }

        return loopDescriptor;
    }
}
