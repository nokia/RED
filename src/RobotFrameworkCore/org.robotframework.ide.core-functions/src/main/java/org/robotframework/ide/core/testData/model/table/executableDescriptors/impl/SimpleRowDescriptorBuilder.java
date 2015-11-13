/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.core.testData.model.table.executableDescriptors.impl;

import java.util.List;

import org.robotframework.ide.core.testData.model.AModelElement;
import org.robotframework.ide.core.testData.model.RobotFile;
import org.robotframework.ide.core.testData.model.table.ARobotSectionTable;
import org.robotframework.ide.core.testData.model.table.RobotExecutableRow;
import org.robotframework.ide.core.testData.model.table.executableDescriptors.IExecutableRowDescriptor;
import org.robotframework.ide.core.testData.model.table.executableDescriptors.IRowDescriptorBuilder;
import org.robotframework.ide.core.testData.model.table.executableDescriptors.RobotAction;
import org.robotframework.ide.core.testData.model.table.executableDescriptors.VariableExtractor;
import org.robotframework.ide.core.testData.model.table.executableDescriptors.ast.mapping.IElementDeclaration;
import org.robotframework.ide.core.testData.model.table.executableDescriptors.ast.mapping.MappingResult;
import org.robotframework.ide.core.testData.model.table.executableDescriptors.ast.mapping.VariableDeclaration;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotToken;


public class SimpleRowDescriptorBuilder implements IRowDescriptorBuilder {

    @Override
    public <T> boolean acceptable(RobotExecutableRow<T> execRowLine) {
        return true;
    }


    @Override
    public <T> IExecutableRowDescriptor<T> buildDescription(
            RobotExecutableRow<T> execRowLine) {
        final SimpleRowDescriptor<T> simple = new SimpleRowDescriptor<>(
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
        boolean isAfterFirstAction = false;
        for (RobotToken elem : lineElements) {
            MappingResult mappingResult = varExtractor.extract(elem, fileName);
            simple.addMessages(mappingResult.getMessages());

            // value is keyword if is on the first place and have in it nested
            // variables and when contains text on the beginning or end of field
            List<VariableDeclaration> correctVariables = mappingResult
                    .getCorrectVariables();
            List<IElementDeclaration> mappedElements = mappingResult
                    .getMappedElements();
            if (isAfterFirstAction) {
                simple.addUsedVariables(correctVariables);
                simple.addTextParameters(mappingResult.getTextElements());
            } else {
                if (correctVariables.size() == 1 && mappedElements.size() == 1) {
                    // definition variable
                    simple.addCreatedVariable(correctVariables.get(0));
                } else {
                    simple.setAction(new RobotAction(elem, mappedElements));
                    simple.addUsedVariables(correctVariables);
                    isAfterFirstAction = true;
                }
            }
        }

        return simple;
    }
}
