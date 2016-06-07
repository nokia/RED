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
                        merge(newExecutionContext, preBuildDescriptors, lineId, previousLineContinue.get());
                    } else {
                        newExecutionContext.add(row);
                    }
                } else if (rowType == ERowType.SIMPLE) {
                    if (containsArtifactalContinueAfectingForLoop(currentExecLine)) {
                        lastForExecutableIndex = newExecutionContext.size();
                        newExecutionContext.add(row);
                    } else {
                        Optional<RobotToken> previousLineContinue = getPreviouseLineContinueToken(
                                row.getElementTokens());
                        if (previousLineContinue.isPresent()) {
                            merge(newExecutionContext, preBuildDescriptors, lineId, previousLineContinue.get());
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

        return newExecutionContext;
    }

    private <T extends AModelElement<? extends ARobotSectionTable>> void merge(
            final List<RobotExecutableRow<T>> newExecutionContext,
            final List<IExecutableRowDescriptor<T>> preBuildDescriptors, final int currentLine,
            final RobotToken previousLineContinueToken) {
        final IExecutableRowDescriptor<T> rowDesc = preBuildDescriptors.get(currentLine);

        RobotExecutableRow<T> toUpdate = null;
        if (newExecutionContext.isEmpty()) {
            toUpdate = new RobotExecutableRow<>();
            newExecutionContext.add(toUpdate);
        } else {
            toUpdate = newExecutionContext.get(newExecutionContext.size() - 1);
        }

        boolean wasComment = false;
        boolean shouldMerge = false;
        final List<RobotToken> elementTokens = rowDesc.getRow().getElementTokens();
        for (final RobotToken rt : elementTokens) {
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
            newExecutionContext.get(line).getAction().getTypes().add(RobotTokenType.FOR_CONTINUE_ARTIFACTAL_TOKEN);
        }
    }

    private Optional<RobotToken> getPreviouseLineContinueToken(final List<RobotToken> tokens) {
        Optional<RobotToken> token = Optional.absent();
        for (final RobotToken rt : tokens) {
            String text = rt.getRaw();
            if (text != null) {
                text = text.trim();
            }
            if (rt.getTypes().contains(RobotTokenType.PREVIOUS_LINE_CONTINUE)) {
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
            if (action.isPresent()) {
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
