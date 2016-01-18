/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.table.exec;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.table.ARobotSectionTable;
import org.rf.ide.core.testdata.model.table.IExecutableStepsHolder;
import org.rf.ide.core.testdata.model.table.RobotExecutableRow;
import org.rf.ide.core.testdata.model.table.exec.ExecutableUnitsFixer.LineCheckDescription.LineFinalDestination;
import org.rf.ide.core.testdata.model.table.exec.descs.IExecutableRowDescriptor;
import org.rf.ide.core.testdata.model.table.exec.descs.IExecutableRowDescriptor.ERowType;
import org.rf.ide.core.testdata.model.table.exec.descs.IExecutableRowDescriptor.IRowType;
import org.rf.ide.core.testdata.model.table.exec.descs.impl.ForLoopContinueRowDescriptor;

import com.google.common.annotations.VisibleForTesting;

public class ExecutableUnitsFixer {

    public <T extends AModelElement<? extends ARobotSectionTable>> void applyFix(
            final IExecutableStepsHolder<T> execUnit) {
        List<RobotExecutableRow<T>> executionContext = execUnit.getExecutionContext();

        System.out.println(execUnit.getHolder().getDeclaration());
        final File processedFile = execUnit.getHolder().getParent().getParent().getParent().getProcessedFile();
        final LineCheckDescription<T> build = build(executionContext, processedFile);
        if (build.isSthToMerge()) {

        }
    }

    @VisibleForTesting
    protected <T extends AModelElement<? extends ARobotSectionTable>> LineCheckDescription<T> build(
            final List<RobotExecutableRow<T>> executionContext, final File processedFile) {
        final LineCheckDescription<T> lineCheckDesc = new LineCheckDescription<>();
        final List<IExecutableRowDescriptor<T>> buildDescs = preBuildDescriptors(executionContext);

        final int execRowsSize = buildDescs.size();
        for (int execLineId = 0; execLineId < execRowsSize; execLineId++) {
            IExecutableRowDescriptor<T> currentExecLineDesc = buildDescs.get(execLineId);
            IRowType rowType = currentExecLineDesc.getRowType();
            System.out.println(rowType);
            if (rowType == ERowType.FOR) {
                lineCheckDesc.addFinalDestinationForLine(LineFinalDestination.NOTHING_TO_DO_JUST_COPY,
                        currentExecLineDesc);
            } else if (rowType == ERowType.FOR_CONTINUE) {
                System.out.println(((ForLoopContinueRowDescriptor) currentExecLineDesc).getKeywordAction().getToken());
                System.out.println(currentExecLineDesc);
            } else if (rowType == ERowType.SIMPLE) {

            } else if (rowType == ERowType.COMMENTED_HASH) {

            } else {
                throw new RuntimeException("RowType " + rowType + " is not supported currently! Last position: "
                        + currentExecLineDesc.getRow().getBeginPosition() + " in file " + processedFile);
            }
        }

        return lineCheckDesc;
    }

    public static class LineCheckDescription<T extends AModelElement<? extends ARobotSectionTable>> {

        private boolean isSthToMerge = false;

        private final List<LineFinalDestination> destinations = new ArrayList<>(0);

        private final List<IExecutableRowDescriptor<T>> descriptors = new ArrayList<>(0);

        public static enum LineFinalDestination {
            NOTHING_TO_DO_JUST_COPY,
            MERGE_FOR_ITEM_CONTINUE,
            MERGE_SIMPLE
        }

        public void addFinalDestinationForLine(final LineFinalDestination dest,
                final IExecutableRowDescriptor<T> execRowDesc) {
            if (dest != LineFinalDestination.NOTHING_TO_DO_JUST_COPY) {
                isSthToMerge = true;
            }

            destinations.add(dest);
            descriptors.add(execRowDesc);
        }

        public boolean isSthToMerge() {
            return isSthToMerge;
        }

        public List<LineFinalDestination> getDestinations() {
            return destinations;
        }

        public List<IExecutableRowDescriptor<T>> getDescriptors() {
            return descriptors;
        }
    }

    private <T extends AModelElement<? extends ARobotSectionTable>> List<IExecutableRowDescriptor<T>> preBuildDescriptors(
            final List<RobotExecutableRow<T>> executionContext) {
        List<IExecutableRowDescriptor<T>> descs = new ArrayList<>(0);

        for (final RobotExecutableRow<T> p : executionContext) {
            descs.add(p.buildLineDescription());
        }

        return descs;
    }
}
