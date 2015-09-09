/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see licence.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.texteditor.utils;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.FindReplaceDocumentAdapter;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.source.SourceViewer;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;

public class TextEditorOccurrenceMarksManager {

    public static final String MARKER_ID = "org.robotframework.ide.texteditor.occurrencesMark";

    private final IFile editedFile;

    private final SourceViewer viewer;

    private FindReplaceDocumentAdapter findAdapter;

    private boolean isMarkVisible;

    public TextEditorOccurrenceMarksManager(final SourceViewer viewer, final IFile editedFile) {
        this.viewer = viewer;
        this.editedFile = editedFile;
    }

    public void showOccurrenceMarks(final int offset) {

        removeOldOccurrenceMarks();

        try {
            final IRegion selectedRegion = new TextEditorHoverManager().findHoveredText(viewer, offset);
            final String selectedText = viewer.getDocument().get(selectedRegion.getOffset(), selectedRegion.getLength());
            findAdapter = new FindReplaceDocumentAdapter(viewer.getDocument());

            final List<IRegion> occurencesRegions = findOccurencesRegions(selectedText);
            final WorkspaceJob wsJob = new WorkspaceJob("Creating occurences markers") {
                @Override
                public IStatus runInWorkspace(final IProgressMonitor monitor) throws CoreException {

                    for (final IRegion region : occurencesRegions) {
                        createMarker(region, selectedText);
                    }
                    return Status.OK_STATUS;
                }
            };
            wsJob.setSystem(true);
            wsJob.schedule();
            wsJob.join();
            isMarkVisible = true;
        } catch (final BadLocationException | InterruptedException e) {
            RedPlugin.logError("Unable to create occurences markers", e);
        }
    }

    private List<IRegion> findOccurencesRegions(final String selectedText) throws BadLocationException {
        final List<IRegion> foundedRegions = new ArrayList<IRegion>();

        IRegion foundedRegion = null;
        int startFindOffset = 0;
        final int variableBegin = selectedText.indexOf("{");
        final int variableEnd = selectedText.indexOf("}");
        if (variableBegin >= 0 && variableEnd >= 0) {
            final String variableName = selectedText.substring(variableBegin + 1, variableEnd);
            while ((foundedRegion = findAdapter.find(startFindOffset, "(.[{]" + variableName + "[}])", true, true,
                    false, true)) != null) {
                startFindOffset = foundedRegion.getOffset() + foundedRegion.getLength();
                foundedRegions.add(foundedRegion);
            }
        } else {
            while ((foundedRegion = findAdapter.find(startFindOffset, selectedText, true, true, true, false)) != null) {
                startFindOffset = foundedRegion.getOffset() + foundedRegion.getLength();
                foundedRegions.add(foundedRegion);
            }
        }
        return foundedRegions;
    }

    public void removeOldOccurrenceMarks() {
        if (isMarkVisible) {
            try {
                editedFile.deleteMarkers(MARKER_ID, true, IResource.DEPTH_ONE);
            } catch (final CoreException e) {
                RedPlugin.logError("Unable to remove occurences markers", e);
            }
            isMarkVisible = false;
        }
    }

    private void createMarker(final IRegion region, final String selectedText) {
        try {
            final IMarker marker = editedFile.createMarker(MARKER_ID);
            marker.setAttribute(IMarker.TRANSIENT, true);
            marker.setAttribute(IMarker.MESSAGE, "Occurrence of '" + selectedText + "'");
            marker.setAttribute(IMarker.CHAR_START, region.getOffset());
            marker.setAttribute(IMarker.CHAR_END, region.getOffset() + region.getLength());
        } catch (final CoreException e) {
            RedPlugin.logError("Unable to create occurences marker for '" + selectedText + "'", e);
        }
    }
}
