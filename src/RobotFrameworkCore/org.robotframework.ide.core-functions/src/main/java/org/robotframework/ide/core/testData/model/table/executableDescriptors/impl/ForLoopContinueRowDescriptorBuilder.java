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
import org.robotframework.ide.core.testData.model.table.IExecutableStepsHolder;
import org.robotframework.ide.core.testData.model.table.RobotExecutableRow;
import org.robotframework.ide.core.testData.model.table.executableDescriptors.IExecutableRowDescriptor;
import org.robotframework.ide.core.testData.model.table.executableDescriptors.IExecutableRowDescriptor.ERowType;
import org.robotframework.ide.core.testData.model.table.executableDescriptors.IExecutableRowDescriptor.IRowType;
import org.robotframework.ide.core.testData.model.table.executableDescriptors.IRowDescriptorBuilder;
import org.robotframework.ide.core.testData.model.table.executableDescriptors.RobotAction;
import org.robotframework.ide.core.testData.model.table.executableDescriptors.VariableExtractor;
import org.robotframework.ide.core.testData.model.table.executableDescriptors.ast.mapping.MappingResult;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotToken;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotTokenType;


public class ForLoopContinueRowDescriptorBuilder implements
        IRowDescriptorBuilder {

    @Override
    public <T> AcceptResult acceptable(final RobotExecutableRow<T> execRowLine) {
        AcceptResult result = new AcceptResult(false);
        String text = execRowLine.getAction().getText().toString();
        if (text != null && !text.trim().isEmpty()) {
            String trimmed = text.trim();
            if (RobotTokenType.FOR_CONTINUE_TOKEN.getRepresentation().get(0)
                    .equalsIgnoreCase(trimmed)) {
                int forLoopDeclarationLine = getForLoopDeclarationLine(execRowLine);
                result = new AcceptResultWithParameters(
                        forLoopDeclarationLine >= 0, forLoopDeclarationLine);
            }
        }

        return result;
    }

    public class AcceptResultWithParameters extends AcceptResult {

        private final int forLoopRowIndex;


        public AcceptResultWithParameters(boolean shouldAccept,
                final int forLoopPosition) {
            super(shouldAccept);
            this.forLoopRowIndex = forLoopPosition;
        }


        public int getForLoopRowIndex() {
            return forLoopRowIndex;
        }
    }


    @Override
    public <T> IExecutableRowDescriptor<T> buildDescription(
            final RobotExecutableRow<T> execRowLine,
            final AcceptResult acceptResult) {
        AcceptResultWithParameters acceptResultWithParams = (AcceptResultWithParameters) acceptResult;
        ForLoopContinueRowDescriptor<T> forContinueDesc = new ForLoopContinueRowDescriptor<>(
                execRowLine);
        forContinueDesc.setForLoopStartRowIndex(acceptResultWithParams
                .getForLoopRowIndex());

        final AModelElement<?> keywordOrTestcase = (AModelElement<?>) execRowLine
                .getParent();
        final ARobotSectionTable table = (ARobotSectionTable) keywordOrTestcase
                .getParent();
        final RobotFile robotFile = (RobotFile) table.getParent();
        final String fileName = robotFile.getParent().getProcessedFile()
                .getAbsolutePath();

        final VariableExtractor varExtractor = new VariableExtractor();
        final List<RobotToken> lineElements = execRowLine.getElementTokens();
        MappingResult mappingResult = varExtractor.extract(
                execRowLine.getAction(), fileName);
        forContinueDesc.addMessages(mappingResult.getMessages());
        forContinueDesc.setAction(new RobotAction(execRowLine.getAction(),
                mappingResult.getMappedElements()));

        mapRestOfForLoopContinue(execRowLine, forContinueDesc, lineElements);

        return forContinueDesc;
    }


    private <T> void mapRestOfForLoopContinue(
            final RobotExecutableRow<T> execRowLine,
            ForLoopContinueRowDescriptor<T> forContinueDesc,
            final List<RobotToken> lineElements) {
        RobotExecutableRow<T> rowWithoutLoopContinue = new RobotExecutableRow<>();
        rowWithoutLoopContinue.setAction(lineElements.get(1));
        rowWithoutLoopContinue.setParent(execRowLine.getParent());
        int size = lineElements.size();
        for (int index = 2; index < size; index++) {
            rowWithoutLoopContinue.addArgument(lineElements.get(index));
        }
        IExecutableRowDescriptor<T> buildDescription = new SimpleRowDescriptorBuilder()
                .buildDescription(rowWithoutLoopContinue,
                        new AcceptResult(true));
        forContinueDesc.setKeywordAction(buildDescription.getAction());
        forContinueDesc.addCreatedVariables(buildDescription
                .getCreatedVariables());
        forContinueDesc.addMessages(buildDescription.getMessages());
        forContinueDesc.addTextParameters(buildDescription.getTextParameters());
        forContinueDesc.addUsedVariables(buildDescription.getUsedVariables());
    }


    @SuppressWarnings("unchecked")
    private <T> int getForLoopDeclarationLine(
            final RobotExecutableRow<T> execRowLine) {
        int forLine = -1;

        IExecutableStepsHolder<AModelElement<? extends ARobotSectionTable>> keywordOrTest = (IExecutableStepsHolder<AModelElement<? extends ARobotSectionTable>>) execRowLine
                .getParent();
        List<RobotExecutableRow<AModelElement<? extends ARobotSectionTable>>> executionContext = keywordOrTest
                .getExecutionContext();
        int myLineNumber = getMyLineIndex(executionContext, execRowLine);
        if (myLineNumber >= 0) {
            for (int lineNumber = myLineNumber - 1; lineNumber >= 0; lineNumber--) {
                RobotExecutableRow<AModelElement<? extends ARobotSectionTable>> row = executionContext
                        .get(lineNumber);
                IExecutableRowDescriptor<AModelElement<? extends ARobotSectionTable>> lineDescription = row
                        .buildLineDescription();
                IRowType rowType = lineDescription.getRowType();
                if (rowType == ERowType.FOR) {
                    forLine = lineNumber;
                    break;
                } else if (rowType == ERowType.FOR_CONTINUE) {
                    forLine = ((ForLoopContinueRowDescriptor<T>) lineDescription)
                            .getForLoopStartRowIndex();
                    break;
                } else if (rowType == ERowType.COMMENTED_HASH
                        || !row.isExecutable()) {
                    continue;
                } else {
                    // is not for
                    break;
                }
            }
        }

        return forLine;
    }


    private <T> int getMyLineIndex(
            final List<RobotExecutableRow<AModelElement<? extends ARobotSectionTable>>> executionContext,
            final RobotExecutableRow<T> execRowLineToFind) {
        int index = -1;
        int size = executionContext.size();
        for (int i = 0; i < size; i++) {
            if (executionContext.get(i) == execRowLineToFind) {
                index = i;
                break;
            }
        }

        return index;
    }
}
