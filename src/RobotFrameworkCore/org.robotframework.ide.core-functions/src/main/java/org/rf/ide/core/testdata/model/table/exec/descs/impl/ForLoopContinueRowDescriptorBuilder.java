/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.table.exec.descs.impl;

import java.util.List;
import java.util.Optional;

import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.FileFormat;
import org.rf.ide.core.testdata.model.RobotFileOutput;
import org.rf.ide.core.testdata.model.table.ARobotSectionTable;
import org.rf.ide.core.testdata.model.table.IExecutableStepsHolder;
import org.rf.ide.core.testdata.model.table.RobotExecutableRow;
import org.rf.ide.core.testdata.model.table.exec.descs.IExecutableRowDescriptor;
import org.rf.ide.core.testdata.model.table.exec.descs.IExecutableRowDescriptor.RowType;
import org.rf.ide.core.testdata.model.table.exec.descs.IRowDescriptorBuilder;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

public class ForLoopContinueRowDescriptorBuilder implements IRowDescriptorBuilder {

    private Optional<Integer> forLoopDeclarationLine = Optional.empty();

    @Override
    public <T> boolean isAcceptable(final RobotExecutableRow<T> execRowLine) {
        final RobotToken action = execRowLine.getAction();
        final String text = action.getText();

        if (text != null && execRowLine.getParent() instanceof IExecutableStepsHolder<?>) {
            if (action.getTypes().contains(RobotTokenType.FOR_WITH_END_CONTINUATION)) {
                final int newForLoopDeclarationLine = getEndTerminatedForLoopDeclarationLine(execRowLine);
                this.forLoopDeclarationLine = Optional.of(newForLoopDeclarationLine);
                return true;
            }

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

    private static <T> int getEndTerminatedForLoopDeclarationLine(final RobotExecutableRow<T> execRowLine) {
        final IExecutableStepsHolder<?> parent = (IExecutableStepsHolder<?>) execRowLine.getParent();
        final List<?> executionContext = parent.getExecutionContext();

        final int index = executionContext.indexOf(execRowLine);
        for (int i = index - 1; i >= 0; i--) {
            final RobotExecutableRow<?> row = (RobotExecutableRow<?>) executionContext.get(i);

            if (row.getAction().getTypes().contains(RobotTokenType.FOR_TOKEN)) {
                return i;
            }
        }
        return -1;
    }

    private static <T> int getForLoopDeclarationLine(final RobotExecutableRow<T> execRowLine) {
        final IExecutableStepsHolder<?> parent = (IExecutableStepsHolder<?>) execRowLine.getParent();
        final List<?> executionContext = parent.getExecutionContext();

        final int index = executionContext.indexOf(execRowLine);
        for (int i = index - 1; i >= 0; i--) {
            final RobotExecutableRow<?> row = (RobotExecutableRow<?>) executionContext.get(i);
            final IExecutableRowDescriptor<?> lineDescription = row.buildLineDescription();
            final RowType rowType = lineDescription.getRowType();
            if (rowType == RowType.FOR) {
                return i;
            } else if (rowType == RowType.FOR_CONTINUE) {
                return ((ForLoopContinueRowDescriptor<?>) lineDescription).getForLoopStartRowIndex();
            } else if (rowType == RowType.COMMENTED_HASH || !row.isExecutable()) {
                continue;
            } else {
                // is not for
                break;
            }
        }
        return -1;
    }

    private <T> boolean isTsv(final RobotExecutableRow<T> execRowLine) {
        final AModelElement<?> execParent = (AModelElement<?>) execRowLine.getParent();
        final ARobotSectionTable table = (ARobotSectionTable) execParent.getParent();
        final RobotFileOutput output = table.getParent().getParent();
        return output.getFileFormat() == FileFormat.TSV;
    }

    @Override
    public <T> IExecutableRowDescriptor<T> buildDescription(final RobotExecutableRow<T> execRowLine) {
        final ForLoopContinueRowDescriptor<T> forContinueDesc = new ForLoopContinueRowDescriptor<>(execRowLine);
        forContinueDesc.setForLoopStartRowIndex(forLoopDeclarationLine.get());
        forContinueDesc.setAction(execRowLine.getAction().copy());

        final List<RobotToken> lineElements = execRowLine.getElementTokens();
        if (lineElements.size() > 1) {
            mapRestOfForLoopContinue(execRowLine, forContinueDesc, lineElements);
        }
        return forContinueDesc;
    }

    private <T> void mapRestOfForLoopContinue(final RobotExecutableRow<T> execRowLine,
            final ForLoopContinueRowDescriptor<T> forContinueDesc, final List<RobotToken> lineElements) {
        final RobotExecutableRow<T> rowWithoutLoopContinue = new RobotExecutableRow<>();
        rowWithoutLoopContinue.setParent(execRowLine.getParent());

        int startIndex = 1;
        final RobotToken action = execRowLine.getAction();
        if ((action.getTypes().contains(RobotTokenType.FOR_CONTINUE_ARTIFICIAL_TOKEN)
                || action.getTypes().contains(RobotTokenType.FOR_WITH_END_CONTINUATION))
                && action.isNotEmpty() && !action.getText().equals("\\")) {
            startIndex = 0;
        }

        boolean foundCommentStart = false;
        for (int index = startIndex; index < lineElements.size(); index++) {
            final RobotToken token = lineElements.get(index);
            if (token.getTypes().contains(RobotTokenType.START_HASH_COMMENT)) {
                foundCommentStart = true;
            }

            if (foundCommentStart) {
                rowWithoutLoopContinue.addCommentPart(token.copy());
            } else if (index == startIndex) {
                rowWithoutLoopContinue.setAction(token.copy());
            } else {
                rowWithoutLoopContinue.addArgument(token.copy());
            }
        }

        final IExecutableRowDescriptor<T> buildDescription = new SimpleRowDescriptorBuilder()
                .buildDescription(rowWithoutLoopContinue);
        forContinueDesc.setKeywordAction(buildDescription.getAction());
        forContinueDesc.addMessages(buildDescription.getMessages());
        forContinueDesc.addTextParameters(buildDescription.getTextParameters());
        forContinueDesc.addCreatedVariables(buildDescription.getCreatedVariables());
        forContinueDesc.addUsedVariables(buildDescription.getUsedVariables());
        forContinueDesc.addKeywordArguments(buildDescription.getKeywordArguments());
    }
}
