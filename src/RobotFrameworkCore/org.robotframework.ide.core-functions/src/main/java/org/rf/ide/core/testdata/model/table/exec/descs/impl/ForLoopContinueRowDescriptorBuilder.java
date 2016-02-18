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
import org.rf.ide.core.testdata.model.table.IExecutableStepsHolder;
import org.rf.ide.core.testdata.model.table.RobotExecutableRow;
import org.rf.ide.core.testdata.model.table.exec.descs.IExecutableRowDescriptor;
import org.rf.ide.core.testdata.model.table.exec.descs.IExecutableRowDescriptor.ERowType;
import org.rf.ide.core.testdata.model.table.exec.descs.IExecutableRowDescriptor.IRowType;
import org.rf.ide.core.testdata.model.table.exec.descs.IRowDescriptorBuilder;
import org.rf.ide.core.testdata.model.table.exec.descs.RobotAction;
import org.rf.ide.core.testdata.model.table.exec.descs.VariableExtractor;
import org.rf.ide.core.testdata.model.table.exec.descs.ast.mapping.MappingResult;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;
import org.rf.ide.core.testdata.text.read.separators.TokenSeparatorBuilder.FileFormat;

public class ForLoopContinueRowDescriptorBuilder implements IRowDescriptorBuilder {

    @Override
    public <T> AcceptResult acceptable(final RobotExecutableRow<T> execRowLine) {
        AcceptResult result = new AcceptResult(false);
        RobotToken action = execRowLine.getAction();
        final String text = action.getText().toString();
        if (text != null) {
            final String trimmed = text.trim();
            if (RobotTokenType.FOR_CONTINUE_TOKEN.getRepresentation().get(0).equalsIgnoreCase(trimmed)
                    || (trimmed.isEmpty() && isTsv(execRowLine))
                    || action.getTypes().contains(RobotTokenType.FOR_CONTINUE_ARTIFACTAL_TOKEN)) {
                final int forLoopDeclarationLine = getForLoopDeclarationLine(execRowLine);
                result = new AcceptResultWithParameters(forLoopDeclarationLine >= 0, forLoopDeclarationLine);
            }
        }

        return result;
    }

    private <T> boolean isTsv(final RobotExecutableRow<T> execRowLine) {
        @SuppressWarnings("unchecked")
        final IExecutableStepsHolder<AModelElement<? extends ARobotSectionTable>> keywordOrTest = (IExecutableStepsHolder<AModelElement<? extends ARobotSectionTable>>) execRowLine
                .getParent();
        AModelElement<? extends ARobotSectionTable> keywordOrTestAsTableElement = keywordOrTest.getHolder();
        ARobotSectionTable table = keywordOrTestAsTableElement.getParent();
        RobotFile model = table.getParent();
        RobotFileOutput output = model.getParent();
        return (output.getFileFormat() == FileFormat.TSV);
    }

    public class AcceptResultWithParameters extends AcceptResult {

        private final int forLoopRowIndex;

        public AcceptResultWithParameters(final boolean shouldAccept, final int forLoopPosition) {
            super(shouldAccept);
            this.forLoopRowIndex = forLoopPosition;
        }

        public int getForLoopRowIndex() {
            return forLoopRowIndex;
        }
    }

    @Override
    public <T> IExecutableRowDescriptor<T> buildDescription(final RobotExecutableRow<T> execRowLine,
            final AcceptResult acceptResult) {
        final AcceptResultWithParameters acceptResultWithParams = (AcceptResultWithParameters) acceptResult;
        final ForLoopContinueRowDescriptor<T> forContinueDesc = new ForLoopContinueRowDescriptor<>(execRowLine);
        forContinueDesc.setForLoopStartRowIndex(acceptResultWithParams.getForLoopRowIndex());

        final AModelElement<?> keywordOrTestcase = (AModelElement<?>) execRowLine.getParent();
        final ARobotSectionTable table = (ARobotSectionTable) keywordOrTestcase.getParent();
        final RobotFile robotFile = table.getParent();
        final String fileName = robotFile.getParent().getProcessedFile().getAbsolutePath();

        final VariableExtractor varExtractor = new VariableExtractor();
        final List<RobotToken> lineElements = execRowLine.getElementTokens();
        final MappingResult mappingResult = varExtractor.extract(execRowLine.getAction(), fileName);
        forContinueDesc.addMessages(mappingResult.getMessages());
        forContinueDesc.setAction(new RobotAction(execRowLine.getAction(), mappingResult.getMappedElements()));

        if (lineElements.size() > 1) {
            mapRestOfForLoopContinue(execRowLine, forContinueDesc, lineElements);
        }

        return forContinueDesc;
    }

    private <T> void mapRestOfForLoopContinue(final RobotExecutableRow<T> execRowLine,
            final ForLoopContinueRowDescriptor<T> forContinueDesc, final List<RobotToken> lineElements) {
        final RobotExecutableRow<T> rowWithoutLoopContinue = new RobotExecutableRow<>();
        boolean mapToComment = false;

        int startIndex = 1;
        if (execRowLine.getAction().getTypes().contains(RobotTokenType.FOR_CONTINUE_ARTIFACTAL_TOKEN)) {
            startIndex = 0;
        }

        RobotToken robotToken = lineElements.get(startIndex);
        if (robotToken.getTypes().contains(RobotTokenType.START_HASH_COMMENT)) {
            mapToComment = true;
            rowWithoutLoopContinue.addComment(robotToken);
        } else {
            rowWithoutLoopContinue.setAction(robotToken);
        }
        rowWithoutLoopContinue.setParent(execRowLine.getParent());
        final int size = lineElements.size();
        for (int index = startIndex + 1; index < size; index++) {
            RobotToken lineElement = lineElements.get(index);
            if (lineElement.getTypes().contains(RobotTokenType.START_HASH_COMMENT)) {
                mapToComment = true;
            }

            if (mapToComment) {
                rowWithoutLoopContinue.addComment(lineElement);
            } else {
                rowWithoutLoopContinue.addArgument(lineElement);
            }
        }
        final IExecutableRowDescriptor<T> buildDescription = new SimpleRowDescriptorBuilder()
                .buildDescription(rowWithoutLoopContinue, new AcceptResult(true));
        forContinueDesc.setKeywordAction(buildDescription.getAction());
        forContinueDesc.addMessages(buildDescription.getMessages());
        forContinueDesc.addTextParameters(buildDescription.getTextParameters());
        forContinueDesc.addCreatedVariables(buildDescription.getCreatedVariables());
        forContinueDesc.addCommentedVariables(buildDescription.getCommentedVariables());
        forContinueDesc.addUsedVariables(buildDescription.getUsedVariables());
        forContinueDesc.addKeywordArguments(buildDescription.getKeywordArguments());
    }

    @SuppressWarnings("unchecked")
    private <T> int getForLoopDeclarationLine(final RobotExecutableRow<T> execRowLine) {
        int forLine = -1;

        final IExecutableStepsHolder<AModelElement<? extends ARobotSectionTable>> keywordOrTest = (IExecutableStepsHolder<AModelElement<? extends ARobotSectionTable>>) execRowLine
                .getParent();
        final List<RobotExecutableRow<AModelElement<? extends ARobotSectionTable>>> executionContext = keywordOrTest
                .getExecutionContext();
        final int myLineNumber = getMyLineIndex(executionContext, execRowLine);
        if (myLineNumber >= 0) {
            for (int lineNumber = myLineNumber - 1; lineNumber >= 0; lineNumber--) {
                final RobotExecutableRow<AModelElement<? extends ARobotSectionTable>> row = executionContext
                        .get(lineNumber);
                final IExecutableRowDescriptor<AModelElement<? extends ARobotSectionTable>> lineDescription = row
                        .buildLineDescription();
                final IRowType rowType = lineDescription.getRowType();
                if (rowType == ERowType.FOR) {
                    forLine = lineNumber;
                    break;
                } else if (rowType == ERowType.FOR_CONTINUE) {
                    forLine = ((ForLoopContinueRowDescriptor<T>) lineDescription).getForLoopStartRowIndex();
                    break;
                } else if (rowType == ERowType.COMMENTED_HASH || !row.isExecutable()) {
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
        final int size = executionContext.size();
        for (int i = 0; i < size; i++) {
            if (executionContext.get(i) == execRowLineToFind) {
                index = i;
                break;
            }
        }

        return index;
    }
}
