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
        ForLoopDeclarationRowDescriptor<T> loopDescriptor = new ForLoopDeclarationRowDescriptor<>(
                execRowLine);

        final AModelElement<?> keywordOrTestcase = (AModelElement<?>) execRowLine
                .getParent();
        final ARobotSectionTable table = (ARobotSectionTable) keywordOrTestcase
                .getParent();
        final RobotFile robotFile = (RobotFile) table.getParent();
        final String fileName = robotFile.getParent().getProcessedFile()
                .getAbsolutePath();

        final VariableExtractor varExtractor = new VariableExtractor();
        final List<RobotToken> lineElements = execRowLine.getElementTokens();
        boolean wasFor = false;
        boolean wasIn = false;
        for (RobotToken elem : lineElements) {
            MappingResult mappingResult = varExtractor.extract(elem, fileName);
            loopDescriptor.addMessages(mappingResult.getMessages());

            // value is keyword if is on the first place and have in it nested
            // variables and when contains text on the beginning or end of field
            List<VariableDeclaration> correctVariables = mappingResult
                    .getCorrectVariables();
            List<IElementDeclaration> mappedElements = mappingResult
                    .getMappedElements();

            if (wasFor) {
                if (wasIn) {
                    loopDescriptor.addUsedVariables(correctVariables);
                    loopDescriptor.addTextParameters(mappingResult
                            .getTextElements());
                } else {
                    if (ForDescriptorInfo.isInToken(elem)) {
                        loopDescriptor.setInAction(new RobotAction(elem,
                                mappedElements));
                        wasIn = true;
                    } else {
                        int variablesSize = correctVariables.size();
                        loopDescriptor.addCreatedVariables(correctVariables);

                        if (!mappingResult.getTextElements().isEmpty()
                                || variablesSize > 1) {
                            BuildMessage errorMessage = BuildMessage
                                    .createErrorMessage(
                                            "Invalid FOR loop variable \'"
                                                    + elem.getText().toString()
                                                    + "\'", fileName);
                            FilePosition startFilePosition = elem
                                    .getFilePosition();
                            FilePosition end = new FilePosition(
                                    startFilePosition.getLine(),
                                    elem.getEndColumn(), elem.getStartOffset()
                                            + elem.getText().length());
                            errorMessage.setFileRegion(new FileRegion(
                                    startFilePosition, end));
                            mappingResult.addBuildMessage(errorMessage);
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

        return loopDescriptor;
    }
}
