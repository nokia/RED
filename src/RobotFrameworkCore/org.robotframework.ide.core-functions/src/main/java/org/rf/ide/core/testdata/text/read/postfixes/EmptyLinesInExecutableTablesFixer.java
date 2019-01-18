/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.read.postfixes;

import java.util.List;

import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.ModelType;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.RobotFileOutput;
import org.rf.ide.core.testdata.model.table.IExecutableStepsHolder;
import org.rf.ide.core.testdata.model.table.RobotEmptyRow;
import org.rf.ide.core.testdata.text.read.IRobotLineElement;
import org.rf.ide.core.testdata.text.read.IRobotTokenType;
import org.rf.ide.core.testdata.text.read.RobotLine;
import org.rf.ide.core.testdata.text.read.postfixes.PostProcessingFixActions.IPostProcessFixer;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

import com.google.common.collect.Range;
import com.google.common.collect.TreeRangeMap;

public class EmptyLinesInExecutableTablesFixer implements IPostProcessFixer {

    @Override
    public void applyFix(final RobotFileOutput parsingOutput) {
        final RobotFile fileModel = parsingOutput.getFileModel();
        if (fileModel.getTestCaseTable().isPresent()) {
            fix(fileModel.getFileContent(), fileModel.getTestCaseTable().getTestCases());
        }
        if (fileModel.getTasksTable().isPresent()) {
            fix(fileModel.getFileContent(), fileModel.getTasksTable().getTasks());
        }
        if (fileModel.getKeywordTable().isPresent()) {
            fix(fileModel.getFileContent(), fileModel.getKeywordTable().getKeywords());
        }
    }

    private void fix(final List<RobotLine> lines,
            final List<? extends IExecutableStepsHolder<?>> execHolders) {
        final TreeRangeMap<Integer, ModelType> linesToModelElements = TreeRangeMap.create();

        for (final IExecutableStepsHolder<?> execHolder : execHolders) {
            for (final AModelElement<?> child : execHolder.getElements()) {
                final Range<Integer> range = Range.closed(child.getBeginPosition().getLine(),
                        child.getEndPosition().getLine());
                linesToModelElements.put(range, child.getModelType());
            }
        }

        for (final IExecutableStepsHolder<?> execHolder : execHolders) {
            final List<?> elements = execHolder.getElements();
            final int holderStartLine = execHolder.getName().getLineNumber();
            final int holderEndLine = getHolderLastLine(execHolder, elements);
            
            if (holderStartLine == -1 || holderEndLine == -1) {
                continue;
            }

            for (int i = holderStartLine; i <= holderEndLine; i++) {
                final RobotLine line = lines.get(i - 1);

                final boolean isEmpty = line.elementsStream()
                        .map(IRobotLineElement::getText)
                        .allMatch(s -> s.trim().isEmpty());
                if (isEmpty && linesToModelElements.get(i) == null) {
                    addEmptyRow(execHolder, elements, i, line);
                }
            }
            for (int i = holderEndLine + 1; i <= lines.size(); i++) {
                final RobotLine line = lines.get(i - 1);
                if (line.getLineElements().isEmpty()) {
                    break;
                }

                final boolean isEmpty = line.elementsStream()
                        .map(IRobotLineElement::getText)
                        .allMatch(s -> s.trim().isEmpty());
                if (isEmpty) {
                    addEmptyRow(execHolder, elements, i, line);
                } else {
                    break;
                }

            }
        }
    }

    private int getHolderLastLine(final IExecutableStepsHolder<?> execHolder, final List<?> elements) {
        if (elements.isEmpty()) {
            return execHolder.getName().getLineNumber();
        }
        final AModelElement<?> lastElement = (AModelElement<?>) elements.get(elements.size() - 1);
        final List<RobotToken> lastElementTokens = lastElement.getElementTokens();
        return lastElementTokens.get(lastElementTokens.size() - 1).getEndFilePosition().getLine();
    }

    private void addEmptyRow(final IExecutableStepsHolder<?> execHolder, final List<?> elements, final int i,
            final RobotLine line) {
        final RobotToken token = line.tokensStream().findFirst().get();
        final List<IRobotTokenType> types = token.getTypes();
        types.add(0, RobotTokenType.EMPTY_CELL);
        types.remove(RobotTokenType.UNKNOWN);

        final RobotEmptyRow<?> row = new RobotEmptyRow<>();
        row.setEmptyToken(token);

        execHolder.addElement(find(elements, i), row);
    }

    private int find(final List<?> elements, final int emptyLineNumber) {
        int i = 0;
        for (final Object element : elements) {
            if (((AModelElement<?>) element).getBeginPosition().getLine() > emptyLineNumber) {
                return i;
            }
            i++;
        }
        return elements.size();
    }
}
