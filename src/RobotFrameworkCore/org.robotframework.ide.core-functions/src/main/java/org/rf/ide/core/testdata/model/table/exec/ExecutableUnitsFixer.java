/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.table.exec;

import java.util.ArrayList;
import java.util.List;

import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.FilePosition;
import org.rf.ide.core.testdata.model.table.ARobotSectionTable;
import org.rf.ide.core.testdata.model.table.IExecutableStepsHolder;
import org.rf.ide.core.testdata.model.table.RobotExecutableRow;
import org.rf.ide.core.testdata.model.table.exec.descs.IExecutableRowDescriptor;
import org.rf.ide.core.testdata.model.table.exec.descs.IExecutableRowDescriptor.ERowType;
import org.rf.ide.core.testdata.model.table.exec.descs.IExecutableRowDescriptor.IRowType;
import org.rf.ide.core.testdata.model.table.exec.descs.RobotAction;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

import com.google.common.base.Optional;

public class ExecutableUnitsFixer {

    public <T extends AModelElement<? extends ARobotSectionTable>> List<RobotExecutableRow<T>> applyFix(
            final IExecutableStepsHolder<T> execUnit) {
        final List<RobotExecutableRow<T>> newExecutionContext = new ArrayList<>(0);

        List<RobotExecutableRow<T>> executionContext = execUnit.getExecutionContext();
        List<IExecutableRowDescriptor<T>> preBuildDescriptors = preBuildDescriptors(executionContext);

        int lastForIndex = -1;
        int lastForExecutableIndex = -1;
        int lineNumbers = preBuildDescriptors.size();
        for (int lineId = 0; lineId < lineNumbers; lineId++) {
            IExecutableRowDescriptor<T> currentExecLine = preBuildDescriptors.get(lineId);
            IRowType rowType = currentExecLine.getRowType();

            RobotExecutableRow<T> row = currentExecLine.getRow();
            if (rowType == ERowType.FOR) {
                if (lastForIndex > -1 && lastForExecutableIndex > -1) {
                    applyArtifactalForLineContinue(newExecutionContext, lastForIndex, lastForExecutableIndex);
                }
                lastForIndex = newExecutionContext.size();
                newExecutionContext.add(row);
                lastForExecutableIndex = -1;
            } else {
                if (rowType == ERowType.FOR_CONTINUE) {
                    Optional<RobotToken> previousLineContinue = getPreviouseLineContinueToken(row.getElementTokens());
                    if (previousLineContinue.isPresent()) {
                        merge(execUnit, newExecutionContext, preBuildDescriptors, lineId, previousLineContinue.get());
                    } else {
                        newExecutionContext.add(row);

                        if (lastForIndex > -1 && lastForExecutableIndex > -1) {
                            lastForExecutableIndex = newExecutionContext.size() - 1;
                            applyArtifactalForLineContinue(newExecutionContext, lastForIndex, lastForExecutableIndex);
                        }
                    }
                } else if (rowType == ERowType.SIMPLE) {
                    if (containsArtifactalContinueAfectingForLoop(currentExecLine)) {
                        lastForExecutableIndex = newExecutionContext.size() - 1;
                        if (lastForIndex == -1) {
                            Optional<Integer> execLine = findLineWithExecAction(newExecutionContext);
                            if (execLine.isPresent()) {
                                final int parentLine = execLine.get();
                                final RobotExecutableRow<T> toMergeLine = newExecutionContext.get(parentLine);
                                final int numberOfMerges = lastForExecutableIndex - parentLine;
                                for (int i = 0; i < numberOfMerges; i++) {
                                    merge(execUnit, toMergeLine, newExecutionContext.get(parentLine + 1));
                                    newExecutionContext.remove(parentLine + 1);
                                }

                                merge(execUnit, toMergeLine, row);
                            } else {
                                newExecutionContext.add(row);
                            }
                        } else {
                            newExecutionContext.add(row);
                            if (lastForIndex > -1 && lastForExecutableIndex > -1) {
                                lastForExecutableIndex = newExecutionContext.size() - 1;
                                applyArtifactalForLineContinue(newExecutionContext, lastForIndex,
                                        lastForExecutableIndex);
                            }
                        }
                    } else {
                        Optional<RobotToken> previousLineContinue = getPreviouseLineContinueToken(
                                row.getElementTokens());
                        if (previousLineContinue.isPresent()) {
                            merge(execUnit, newExecutionContext, preBuildDescriptors, lineId,
                                    previousLineContinue.get());
                        } else {
                            newExecutionContext.add(row);
                        }

                        if (lastForIndex > -1 && lastForExecutableIndex > -1) {
                            applyArtifactalForLineContinue(newExecutionContext, lastForIndex, lastForExecutableIndex);
                        }
                        lastForIndex = -1;
                        lastForExecutableIndex = -1;
                    }
                } else if (rowType == ERowType.COMMENTED_HASH) {
                    newExecutionContext.add(row);
                } else {
                    throw new IllegalStateException("Unsupported executable row type " + rowType + ". In file "
                            + execUnit.getHolder().getParent().getParent().getParent().getProcessedFile() + " near "
                            + row.getBeginPosition());
                }
            }

        }

        if (lastForIndex > -1 && lastForExecutableIndex > -1) {
            applyArtifactalForLineContinue(newExecutionContext, lastForIndex, lastForExecutableIndex);
        }

        boolean isContinue = false;
        int size = newExecutionContext.size();
        for (int i = size - 1; i >= 0; i--) {
            final RobotExecutableRow<T> execLine = newExecutionContext.get(i);
            final IExecutableRowDescriptor<T> lineDescription = execLine.buildLineDescription();
            final IRowType rowType = lineDescription.getRowType();
            if (rowType == ERowType.FOR_CONTINUE) {
                if (execLine.getAction().getText().isEmpty()) {
                    if (execLine.getAction().getTypes().contains(RobotTokenType.FOR_CONTINUE_ARTIFICIAL_TOKEN)) {
                        if (isActionNotTheFirstElement(execLine.getAction(), execLine.getElementTokens())) {
                            RobotToken actionToBeArgument = execLine.getAction().copy();
                            actionToBeArgument.getTypes().remove(RobotTokenType.KEYWORD_ACTION_NAME);
                            actionToBeArgument.getTypes().remove(RobotTokenType.TEST_CASE_ACTION_NAME);
                            actionToBeArgument.getTypes().remove(RobotTokenType.FOR_CONTINUE_ARTIFICIAL_TOKEN);
                            actionToBeArgument.setRaw("\\");
                            actionToBeArgument.setText("\\");
                            execLine.addArgument(0, actionToBeArgument);
                        }
                        execLine.getAction().setText("\\");
                        execLine.getAction().setRaw("\\");
                    } else {
                        execLine.getAction().setText("\\");
                        execLine.getAction().setRaw("\\");
                        execLine.getAction().getTypes().add(RobotTokenType.FOR_CONTINUE_ARTIFICIAL_TOKEN);
                    }
                } else if (!execLine.getAction().getText().equals("\\")) {
                    RobotToken actionToBeArgument = execLine.getAction().copy();
                    actionToBeArgument.getTypes().remove(RobotTokenType.KEYWORD_ACTION_NAME);
                    actionToBeArgument.getTypes().remove(RobotTokenType.TEST_CASE_ACTION_NAME);
                    actionToBeArgument.getTypes().remove(RobotTokenType.FOR_CONTINUE_ARTIFICIAL_TOKEN);
                    execLine.addArgument(0, actionToBeArgument);
                    execLine.getAction().setText("\\");
                    execLine.getAction().setRaw("\\");
                    execLine.getAction().getTypes().add(RobotTokenType.FOR_CONTINUE_ARTIFICIAL_TOKEN);
                } else {
                    execLine.getAction().getTypes().remove(RobotTokenType.FOR_CONTINUE_ARTIFICIAL_TOKEN);
                }
                isContinue = true;
            } else if (rowType == ERowType.COMMENTED_HASH) {
                if (isContinue) {
                    if (!execLine.getAction().getText().equals("\\")) {
                        execLine.getAction().setText("\\");
                        execLine.getAction().setRaw("\\");
                        execLine.getAction().getTypes().add(RobotTokenType.FOR_CONTINUE_ARTIFICIAL_TOKEN);
                    }
                }
            } else if (rowType == ERowType.FOR || rowType == ERowType.SIMPLE) {
                isContinue = false;
            }
        }

        return newExecutionContext;
    }

    /**
     * Special hanlding for TSV format:
     * <code>
     *  :FOR    ${x}    IN  10
        #   d
        ...     kw_w
        \   kw_w
     * </code>
     * 
     * @param action
     * @param elementTokens
     * @return
     */
    private boolean isActionNotTheFirstElement(final RobotToken action, final List<RobotToken> elementTokens) {
        final FilePosition actionPosition = action.getFilePosition();
        if (!actionPosition.isNotSet()) {
            for (final RobotToken tok : elementTokens) {
                final FilePosition currentTokenPosition = tok.getFilePosition();
                if (!currentTokenPosition.isNotSet()) {
                    if (currentTokenPosition.isBefore(actionPosition)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    private <T> Optional<Integer> findLineWithExecAction(final List<RobotExecutableRow<T>> newExecutionContext) {
        for (int i = newExecutionContext.size() - 1; i >= 0; i--) {
            if (newExecutionContext.get(i).isExecutable()) {
                return Optional.of(i);
            }
        }
        return Optional.absent();
    }

    private <T extends AModelElement<? extends ARobotSectionTable>> void merge(final IExecutableStepsHolder<T> execUnit,
            final RobotExecutableRow<T> outputLine, final RobotExecutableRow<T> toMerge) {
        if (!toMerge.getAction().getFilePosition().isNotSet()) {
            outputLine.addArgument(toMerge.getAction());
        }

        List<RobotToken> arguments = toMerge.getArguments();
        for (RobotToken t : arguments) {
            outputLine.addArgument(t);
        }

        List<RobotToken> comments = toMerge.getComment();
        for (RobotToken robotToken : comments) {
            outputLine.addCommentPart(robotToken);
        }
    }

    @SuppressWarnings("unchecked")
    private <T extends AModelElement<? extends ARobotSectionTable>> void merge(final IExecutableStepsHolder<T> execUnit,
            final List<RobotExecutableRow<T>> newExecutionContext,
            final List<IExecutableRowDescriptor<T>> preBuildDescriptors, final int currentLine,
            final RobotToken previousLineContinueToken) {
        final IExecutableRowDescriptor<T> rowDesc = preBuildDescriptors.get(currentLine);

        RobotExecutableRow<T> toUpdate = null;
        if (newExecutionContext.isEmpty()) {
            toUpdate = new RobotExecutableRow<>();
            toUpdate.setParent((T) execUnit);
            newExecutionContext.add(toUpdate);
        } else {
            toUpdate = newExecutionContext.get(newExecutionContext.size() - 1);
        }

        boolean wasComment = false;
        boolean shouldMerge = false;
        final List<RobotToken> elementTokens = rowDesc.getRow().getElementTokens();
        final int size = elementTokens.size();
        for (int i = 0; i < size; i++) {
            RobotToken rt = elementTokens.get(i);
            if (rowDesc.getRowType() == ERowType.FOR_CONTINUE && toUpdate.getAction().getFilePosition().isNotSet()
                    && "\\".equals(rt.getText())) {
                toUpdate.setAction(rt);
            }

            if (rt == previousLineContinueToken) {
                shouldMerge = true;
                continue;
            }

            if (shouldMerge) {
                if (rt.getTypes().contains(RobotTokenType.START_HASH_COMMENT) || wasComment) {
                    wasComment = true;
                    toUpdate.addCommentPart(rt);
                } else {
                    if (toUpdate.getAction().getFilePosition().isNotSet()) {
                        toUpdate.setAction(rt);
                    } else {
                        toUpdate.addArgument(rt);
                    }
                }
            }
        }
    }

    private <T extends AModelElement<? extends ARobotSectionTable>> void applyArtifactalForLineContinue(
            final List<RobotExecutableRow<T>> newExecutionContext, final int lastForIndex,
            final int lastForExecutableIndex) {
        for (int line = lastForIndex + 1; line <= lastForExecutableIndex; line++) {
            newExecutionContext.get(line).getAction().getTypes().add(RobotTokenType.FOR_CONTINUE_ARTIFICIAL_TOKEN);
        }
    }

    private Optional<RobotToken> getPreviouseLineContinueToken(final List<RobotToken> tokens) {
        Optional<RobotToken> token = Optional.absent();
        for (final RobotToken rt : tokens) {
            String text = rt.getRaw();
            if (text != null) {
                text = text.trim();
            }
            if (rt.getTypes().contains(RobotTokenType.PREVIOUS_LINE_CONTINUE) && "...".equals(rt.getText().trim())) {
                token = Optional.of(rt);
            } else if (text != null) {
                if (text.equals("\\") || text.isEmpty()) {
                    continue;
                } else {
                    break;
                }
            }
        }

        return token;
    }

    private <T extends AModelElement<? extends ARobotSectionTable>> List<IExecutableRowDescriptor<T>> preBuildDescriptors(
            final List<RobotExecutableRow<T>> executionContext) {
        List<IExecutableRowDescriptor<T>> descs = new ArrayList<>(0);

        for (final RobotExecutableRow<T> p : executionContext) {
            descs.add(p.buildLineDescription());
        }

        return descs;
    }

    private <T extends AModelElement<? extends ARobotSectionTable>> boolean containsArtifactalContinueAfectingForLoop(
            final IExecutableRowDescriptor<T> execRow) {
        boolean result = false;

        if (execRow.getRowType() == ERowType.SIMPLE) {
            RobotAction action = execRow.getAction();
            if (action.isPresent() || !action.getToken().getFilePosition().isNotSet()) {
                RobotToken actionToken = action.getToken();
                FilePosition actionTokenPos = actionToken.getFilePosition();
                List<RobotToken> elementTokens = execRow.getRow().getElementTokens();
                if (!actionTokenPos.isNotSet()) {
                    for (final RobotToken rt : elementTokens) {
                        if (rt != actionToken) {
                            if (rt.getLineNumber() < actionToken.getLineNumber()) {
                                result = true;
                                break;
                            }
                        }
                    }
                }
            }
        }

        return result;
    }
}
