/*
* Copyright 2017 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.robotframework.ide.eclipse.main.plugin.refactoring;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IDocument;
import org.rf.ide.core.testdata.model.FilePosition;
import org.rf.ide.core.testdata.model.FileRegion;
import org.rf.ide.core.testdata.text.read.LineReader;

import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;

class TextOperations {

    /**
     * Given the region in file which is known only by line column translates to the region
     * with properly set offsets.
     * 
     * @param region
     * @param file
     * @return
     */
    static FileRegion getAffectedRegion(final FileRegion region, final IFile file) {
        return offsetOfRegion(region, lineOffsets(file));
    }

    /**
     * Given the region in document which is known only by line column translates to the region
     * with properly set offsets.
     * 
     * @param fileRegion
     * @param document
     * @return
     */
    static FileRegion getAffectedRegion(final FileRegion fileRegion, final IDocument document) {
        return offsetOfRegion(fileRegion,
                lineOffsets(new ByteArrayInputStream(document.get().getBytes(Charsets.UTF_8))));
    }

    private static List<FileRegion> lineOffsets(final IFile file) {
        try {
            return lineOffsets(file.getContents());
        } catch (final CoreException e) {
            return new ArrayList<>();
        }
    }

    private static List<FileRegion> lineOffsets(final InputStream stream) {
        try (final LineReader lineReader = new LineReader(new InputStreamReader(stream, Charsets.UTF_8));
                final BufferedReader reader = new BufferedReader(lineReader)) {

            while (reader.readLine() != null) {
                // just read everything so that lineReader will know line offsets
            }
            return lineReader.getLinesRegion();

        } catch (final IOException e) {
            return new ArrayList<>();
        }
    }

    private static FileRegion offsetOfRegion(final FileRegion region, final List<FileRegion> allRegions) {
        Preconditions.checkArgument(region.getStart().getLine() != FilePosition.NOT_SET);
        Preconditions.checkArgument(region.getStart().getColumn() != FilePosition.NOT_SET);
        Preconditions.checkArgument(region.getStart().getOffset() == FilePosition.NOT_SET);
        Preconditions.checkArgument(region.getEnd().getLine() != FilePosition.NOT_SET);
        Preconditions.checkArgument(region.getEnd().getColumn() != FilePosition.NOT_SET);
        Preconditions.checkArgument(region.getEnd().getOffset() == FilePosition.NOT_SET);

        final FilePosition startOfBeginLine = allRegions.get(region.getStart().getLine() - 1).getStart();
        final FilePosition endOfEndLinePosition = allRegions.get(region.getEnd().getLine() - 1).getEnd();

        final int startOffset = startOfBeginLine.getOffset() + region.getStart().getColumn() - 1;
        final int endOffset = endOfEndLinePosition.getOffset()
                - (endOfEndLinePosition.getColumn() - region.getEnd().getColumn() + 1);
        return new FileRegion(new FilePosition(region.getStart().getLine(), region.getStart().getColumn(), startOffset),
                new FilePosition(region.getEnd().getLine(), region.getEnd().getColumn(), endOffset));
    }
}
