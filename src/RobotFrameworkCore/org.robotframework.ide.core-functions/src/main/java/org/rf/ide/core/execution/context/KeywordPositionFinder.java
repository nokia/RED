/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.execution.context;

import java.util.List;

import org.rf.ide.core.testdata.importer.ResourceImportReference;
import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.RobotFileOutput;
import org.rf.ide.core.testdata.model.RobotFileOutput.RobotFileType;
import org.rf.ide.core.testdata.model.table.ARobotSectionTable;
import org.rf.ide.core.testdata.model.table.RobotExecutableRow;
import org.rf.ide.core.testdata.model.table.exec.descs.IExecutableRowDescriptor;
import org.rf.ide.core.testdata.model.table.exec.descs.IExecutableRowDescriptor.ERowType;
import org.rf.ide.core.testdata.model.table.exec.descs.impl.ForLoopContinueRowDescriptor;

/**
 * @author bembenek
 */
public class KeywordPositionFinder {

    private final List<KeywordContext> currentKeywords;

    private final IRobotExecutableRowFinder executableRowFinder;

    public KeywordPositionFinder(final List<KeywordContext> currentKeywords,
            final IRobotExecutableRowFinder executableRowFinder) {
        this.currentKeywords = currentKeywords;
        this.executableRowFinder = executableRowFinder;
    }

    public KeywordPosition find() {
        final RobotExecutableRow<?> executableRow = findExecutableRow(executableRowFinder);
        if (executableRow == null) {
            return new KeywordPosition(findFirstResourceImportPath(), -1);
        }
        return new KeywordPosition(findPath(executableRow), findLine(executableRow));
    }

    private RobotExecutableRow<?> findExecutableRow(final IRobotExecutableRowFinder executableRowFinder) {
        if (executableRowFinder != null) {
            return executableRowFinder.findExecutableRow(currentKeywords);
        }
        return null;
    }

    private String findPath(final RobotExecutableRow<?> executableRow) {
        @SuppressWarnings("unchecked")
        final RobotExecutableRow<AModelElement<?>> element = (RobotExecutableRow<AModelElement<?>>) executableRow;
        final ARobotSectionTable table = (ARobotSectionTable) element.getParent().getParent();
        final RobotFileOutput robotFileOutput = table.getParent().getParent();
        if (robotFileOutput.getType() == RobotFileType.RESOURCE) {
            return findFirstResourceImportPath();
        }
        return robotFileOutput.getProcessedFile().getAbsolutePath();
    }

    private int findLine(final RobotExecutableRow<?> executableRow) {
        final IExecutableRowDescriptor<?> buildLineDescription = executableRow.buildLineDescription();
        if (buildLineDescription.getRowType() == ERowType.FOR_CONTINUE) {
            return ((ForLoopContinueRowDescriptor<?>) buildLineDescription).getKeywordAction()
                    .getToken()
                    .getLineNumber();
        }
        return executableRow.getAction().getLineNumber();
    }

    private String findFirstResourceImportPath() {
        for (int i = currentKeywords.size() - 1; i >= 0; i--) {
            final ResourceImportReference resImport = currentKeywords.get(i).getResourceImportReference();
            if (resImport != null) {
                return resImport.getReference().getProcessedFile().getAbsolutePath();
            }
        }
        return null;
    }
}
