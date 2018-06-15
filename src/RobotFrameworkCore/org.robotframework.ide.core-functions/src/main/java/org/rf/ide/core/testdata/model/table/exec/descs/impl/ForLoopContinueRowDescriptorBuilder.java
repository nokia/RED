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

import com.google.common.base.Optional;

public class ForLoopContinueRowDescriptorBuilder implements IRowDescriptorBuilder {

    private Optional<Integer> forLoopDeclarationLine = Optional.absent();

    @Override
    public <T> boolean isAcceptable(final RobotExecutableRow<T> execRowLine) {
        final RobotToken action = execRowLine.getAction();
        final String text = action.getText();

        if (text != null && execRowLine.getParent() instanceof IExecutableStepsHolder<?>) {
            final String trimmed = text.trim();
            if (RobotTokenType.FOR_CONTINUE_TOKEN.getRepresentation().get(0).equalsIgnoreCase(trimmed)
                    || (trimmed.isEmpty() && isTsv(execRowLine))
                    || action.getTypes().contains(RobotTokenType.FOR_CONTINUE_ARTIFICIAL_TOKEN)) {

                final int forLoopDeclarationLine = getForLoopDeclarationLine(execRowLine);
                this.forLoopDeclarationLine = Optional.of(forLoopDeclarationLine);
                return forLoopDeclarationLine >= 0;
            }
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    private <T> int getForLoopDeclarationLine(final RobotExecutableRow<T> execRowLine) {
        final IExecutableStepsHolder<AModelElement<? extends ARobotSectionTable>> keywordOrTest =
                (IExecutableStepsHolder<AModelElement<? extends ARobotSectionTable>>) execRowLine.getParent();
        final List<RobotExecutableRow<AModelElement<? extends ARobotSectionTable>>> executionContext =
                keywordOrTest.getExecutionContext();

        final int index = executionContext.indexOf(execRowLine);
        for (int i = index - 1; i >= 0; i--) {
            final RobotExecutableRow<AModelElement<? extends ARobotSectionTable>> row = executionContext
                    .get(i);
            final IExecutableRowDescriptor<AModelElement<? extends ARobotSectionTable>> lineDescription = row
                    .buildLineDescription();
            final IRowType rowType = lineDescription.getRowType();
            if (rowType == ERowType.FOR) {
                return i;
            } else if (rowType == ERowType.FOR_CONTINUE) {
                return ((ForLoopContinueRowDescriptor<T>) lineDescription).getForLoopStartRowIndex();
            } else if (rowType == ERowType.COMMENTED_HASH || !row.isExecutable()) {
                continue;
            } else {
                // is not for
                break;
            }
        }
        return -1;
    }

    private <T> boolean isTsv(final RobotExecutableRow<T> execRowLine) {
        @SuppressWarnings("unchecked")
        final IExecutableStepsHolder<AModelElement<? extends ARobotSectionTable>> keywordOrTest =
            (IExecutableStepsHolder<AModelElement<? extends ARobotSectionTable>>) execRowLine.getParent();
        final AModelElement<? extends ARobotSectionTable> keywordOrTestAsTableElement = keywordOrTest.getHolder();
        final ARobotSectionTable table = keywordOrTestAsTableElement.getParent();
        final RobotFile model = table.getParent();
        final RobotFileOutput output = model.getParent();
        return output.getFileFormat() == FileFormat.TSV;
    }

    @Override
    public <T> IExecutableRowDescriptor<T> buildDescription(final RobotExecutableRow<T> execRowLine) {
        final ForLoopContinueRowDescriptor<T> forContinueDesc = new ForLoopContinueRowDescriptor<>(execRowLine);
        forContinueDesc.setForLoopStartRowIndex(forLoopDeclarationLine.get());

        final AModelElement<?> keywordOrTestcase = (AModelElement<?>) execRowLine.getParent();
        final ARobotSectionTable table = (ARobotSectionTable) keywordOrTestcase.getParent();
        final RobotFile robotFile = table.getParent();
        final String fileName = robotFile.getParent().getProcessedFile().getAbsolutePath();

        final VariableExtractor varExtractor = new VariableExtractor();
        final List<RobotToken> lineElements = execRowLine.getElementTokens();
        final MappingResult mappingResult = varExtractor.extract(execRowLine.getAction(), fileName);
        forContinueDesc.addMessages(mappingResult.getMessages());
        forContinueDesc.setAction(new RobotAction(execRowLine.getAction().copy(), mappingResult.getMappedElements()));

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
        final RobotToken action = execRowLine.getAction();
        if (action.getTypes().contains(RobotTokenType.FOR_CONTINUE_ARTIFICIAL_TOKEN)
                && action.isNotEmpty() && !action.getText().equals("\\")) {
            startIndex = 0;
        }

        final RobotToken robotToken = lineElements.get(startIndex);
        if (robotToken.getTypes().contains(RobotTokenType.START_HASH_COMMENT)) {
            mapToComment = true;
            rowWithoutLoopContinue.addCommentPart(robotToken.copy());
        } else {
            rowWithoutLoopContinue.setAction(robotToken.copy());
        }
        rowWithoutLoopContinue.setParent(execRowLine.getParent());
        final int size = lineElements.size();
        for (int index = startIndex + 1; index < size; index++) {
            final RobotToken lineElement = lineElements.get(index);
            if (lineElement.getTypes().contains(RobotTokenType.START_HASH_COMMENT)) {
                mapToComment = true;
            }

            if (mapToComment) {
                rowWithoutLoopContinue.addCommentPart(lineElement);
            } else {
                rowWithoutLoopContinue.addArgument(lineElement);
            }
        }
        final IExecutableRowDescriptor<T> buildDescription = new SimpleRowDescriptorBuilder()
                .buildDescription(rowWithoutLoopContinue);
        forContinueDesc.setKeywordAction(buildDescription.getAction());
        forContinueDesc.addMessages(buildDescription.getMessages());
        forContinueDesc.addTextParameters(buildDescription.getTextParameters());
        forContinueDesc.addCreatedVariables(buildDescription.getCreatedVariables());
        forContinueDesc.addCommentedVariables(buildDescription.getCommentedVariables());
        forContinueDesc.addUsedVariables(buildDescription.getUsedVariables());
        forContinueDesc.addKeywordArguments(buildDescription.getKeywordArguments());
    }
}
