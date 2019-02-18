/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.read.postfixes;

import java.util.List;

import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.FileFormat;
import org.rf.ide.core.testdata.model.ModelType;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.RobotFileOutput;
import org.rf.ide.core.testdata.model.table.ARobotSectionTable;
import org.rf.ide.core.testdata.model.table.IExecutableStepsHolder;
import org.rf.ide.core.testdata.model.table.RobotEmptyRow;
import org.rf.ide.core.testdata.model.table.RobotExecutableRow;
import org.rf.ide.core.testdata.text.read.RobotLine;
import org.rf.ide.core.testdata.text.read.postfixes.PostProcessingFixActions.IPostProcessFixer;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;
import org.rf.ide.core.testdata.text.read.separators.Separator;

import com.google.common.collect.Range;
import com.google.common.collect.TreeRangeMap;

public class EmptyLinesInExecutableTablesFixer implements IPostProcessFixer {

    @Override
    public void applyFix(final RobotFileOutput parsingOutput) {
        final RobotFile fileModel = parsingOutput.getFileModel();
        if (fileModel.getTestCaseTable().isPresent()) {
            fix(parsingOutput.getFileFormat(), fileModel.getFileContent(), fileModel.getTestCaseTable().getTestCases());
        }
        if (fileModel.getTasksTable().isPresent()) {
            fix(parsingOutput.getFileFormat(), fileModel.getFileContent(), fileModel.getTasksTable().getTasks());
        }
        if (fileModel.getKeywordTable().isPresent()) {
            fix(parsingOutput.getFileFormat(), fileModel.getFileContent(), fileModel.getKeywordTable().getKeywords());
        }
    }

    private <T extends AModelElement<? extends ARobotSectionTable>> void fix(final FileFormat fileFormat,
            final List<RobotLine> lines, final List<? extends IExecutableStepsHolder<T>> execHolders) {
        createEmptyLinesForLinesNotBelongingToAnyModelElement(lines, execHolders);
        exchangeEmptyExecutableRowsWithEmptyRow(fileFormat, execHolders);
    }

    private <T extends AModelElement<? extends ARobotSectionTable>> void createEmptyLinesForLinesNotBelongingToAnyModelElement(
            final List<RobotLine> lines,
            final List<? extends IExecutableStepsHolder<T>> execHolders) {

        final TreeRangeMap<Integer, ModelType> linesToModelElements = TreeRangeMap.create();

        for (final IExecutableStepsHolder<T> execHolder : execHolders) {
            for (final AModelElement<T> child : execHolder.getElements()) {
                final Range<Integer> range = Range.closed(child.getBeginPosition().getLine(),
                        child.getEndPosition().getLine());
                linesToModelElements.put(range, child.getModelType());
            }
        }

        for (final IExecutableStepsHolder<T> execHolder : execHolders) {
            final List<AModelElement<T>> elements = execHolder.getElements();
            final int holderStartLine = execHolder.getName().getLineNumber();
            final int holderEndLine = getHolderLastLine(execHolder, elements);
            
            if (holderStartLine == -1 || holderEndLine == -1) {
                continue;
            }

            for (int i = holderStartLine + 1; i <= holderEndLine; i++) {
                final RobotLine line = lines.get(i - 1);

                if (linesToModelElements.get(i) == null && line.isEmpty()) {
                    final RobotEmptyRow<T> emptyRow = createEmptyRow(line);
                    if (emptyRow != null) {
                        final int index = find(elements, i);
                        execHolder.addElement(index, emptyRow);
                    }
                }
            }
            for (int i = holderEndLine + 1; i <= lines.size(); i++) {
                final RobotLine line = lines.get(i - 1);
                if (line.getLineElements().isEmpty()) {
                    break;
                }
                if (line.isEmpty()) {
                    final RobotEmptyRow<T> emptyRow = createEmptyRow(line);
                    if (emptyRow != null) {
                        final int index = find(elements, i);
                        execHolder.addElement(index, emptyRow);
                    }
                } else {
                    break;
                }

            }
        }
    }

    private <T extends AModelElement<? extends ARobotSectionTable>> int getHolderLastLine(
            final IExecutableStepsHolder<?> execHolder, final List<AModelElement<T>> elements) {
        if (elements.isEmpty()) {
            return execHolder.getName().getLineNumber();
        }
        final AModelElement<?> lastElement = elements.get(elements.size() - 1);
        final List<RobotToken> lastElementTokens = lastElement.getElementTokens();
        return lastElementTokens.get(lastElementTokens.size() - 1).getEndFilePosition().getLine();
    }

    private <T extends AModelElement<? extends ARobotSectionTable>> RobotEmptyRow<T> createEmptyRow(
            final RobotLine line) {

        if (line.getLineElements().size() == 1
                && line.getLineElements().get(0).getTypes().contains(RobotTokenType.PRETTY_ALIGN_SPACE)) {

            final RobotToken prettyAlign = (RobotToken) line.getLineElements().get(0);

            final RobotToken token = RobotToken.create("", RobotTokenType.EMPTY_CELL);
            token.setLineNumber(line.getLineNumber());
            token.setStartOffset(prettyAlign.getEndOffset());
            token.setStartColumn(prettyAlign.getEndColumn());

            line.addLineElement(token);

            final RobotEmptyRow<T> row = new RobotEmptyRow<>();
            row.setEmpty(token);

            return row;

        } else if (line.getLineElements().get(0) instanceof Separator) {
            final Separator separator = (Separator) line.getLineElements().get(0);

            final RobotToken token = RobotToken.create("", RobotTokenType.EMPTY_CELL);
            token.setLineNumber(line.getLineNumber());
            token.setStartOffset(separator.getEndOffset());
            token.setStartColumn(separator.getEndColumn());

            line.addLineElement(token);

            final RobotEmptyRow<T> row = new RobotEmptyRow<>();
            row.setEmpty(token);

            return row;
        } else {
            return null;
        }
    }

    private <T extends AModelElement<? extends ARobotSectionTable>> int find(final List<AModelElement<T>> elements,
            final int emptyLineNumber) {
        int i = 0;
        for (final AModelElement<T> element : elements) {
            if (element.getBeginPosition().getLine() > emptyLineNumber) {
                return i;
            }
            i++;
        }
        return elements.size();
    }

    private <T extends AModelElement<? extends ARobotSectionTable>> void exchangeEmptyExecutableRowsWithEmptyRow(
            final FileFormat fileFormat,
            final List<? extends IExecutableStepsHolder<T>> execHolders) {

        for (final IExecutableStepsHolder<T> holder : execHolders) {
            final List<RobotExecutableRow<T>> executionRows = holder.getExecutionContext();

            for (final RobotExecutableRow<T> executableRow : executionRows) {
                if (!RobotExecutableRow.isExecutable(fileFormat, executableRow.getElementTokens())) {
                    final RobotEmptyRow<T> row = rewriteToEmptyRow(executableRow);
                    
                    holder.replaceElement(executableRow, row);
                }
            }
        }
    }

    private <T extends AModelElement<? extends ARobotSectionTable>> RobotEmptyRow<T> rewriteToEmptyRow(
            final RobotExecutableRow<T> executableRow) {

        final RobotEmptyRow<T> emptyRow = new RobotEmptyRow<>();
        emptyRow.setEmpty(executableRow.getAction());
        for (final RobotToken commentToken : executableRow.getComment()) {
            emptyRow.addCommentPart(commentToken);
        }
        return emptyRow;
    }
}
