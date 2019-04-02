/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.source.formatter;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.compare.rangedifferencer.IRangeComparator;
import org.eclipse.compare.rangedifferencer.RangeDifference;
import org.eclipse.compare.rangedifferencer.RangeDifferencer;
import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.filebuffers.ITextFileBufferManager;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.text.IDocument;

/**
 * Based on org.eclipse.jdt.internal.ui.javaeditor.EditorUtility.calculateChangedLineRegions
 */
public class SuiteSourceEditorDifferenceFinder {

    public static List<Integer> calculateChangedLines(final IFile file, final IDocument currentDocument,
            final IProgressMonitor monitor) {
        final IFileStore fileStore = FileBuffers.getFileStoreAtLocation(file.getLocation());
        final ITextFileBufferManager fileBufferManager = FileBuffers.createTextFileBufferManager();
        try {
            fileBufferManager.connectFileStore(fileStore, SubMonitor.convert(monitor, 15));
            try {
                final IDocument oldDocument = ((ITextFileBuffer) fileBufferManager.getFileStoreFileBuffer(fileStore))
                        .getDocument();
                return calculateChangedLines(oldDocument, currentDocument);
            } finally {
                fileBufferManager.disconnectFileStore(fileStore, SubMonitor.convert(monitor, 15));
            }
        } catch (final CoreException e) {
            return new ArrayList<>();
        }
    }

    private static List<Integer> calculateChangedLines(final IDocument oldDocument, final IDocument currentDocument) {
        final IRangeComparator leftSide = new SuiteSourceEditorLineComparator(oldDocument);
        final IRangeComparator rightSide = new SuiteSourceEditorLineComparator(currentDocument);
        final RangeDifference[] differences = RangeDifferencer.findDifferences(leftSide, rightSide);

        //It holds that:
        //1. Ranges are sorted:
        //     forAll r1,r2 element differences: indexOf(r1)<indexOf(r2) -> r1.rightStart()<r2.rightStart();
        //2. Successive changed lines are merged into on RangeDifference
        //     forAll r1,r2 element differences: r1.rightStart()<r2.rightStart() -> r1.rightEnd()<r2.rightStart

        final List<Integer> regions = new ArrayList<>();
        for (final RangeDifference difference : differences) {
            if (difference.kind() == RangeDifference.CHANGE && difference.rightLength() > 0) {
                final int startLine = difference.rightStart();
                final int endLine = difference.rightEnd() - 1;

                if (startLine == endLine) {
                    regions.add(startLine);
                } else {
                    for (int iLine = startLine; iLine <= endLine; iLine++) {
                        regions.add(iLine);
                    }
                }
            }
        }
        return regions;
    }

}
