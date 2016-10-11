/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.source;

import java.util.Objects;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.swt.custom.CaretEvent;
import org.eclipse.swt.custom.CaretListener;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;

import com.google.common.base.Optional;

/**
 * @author Michal Anglart
 */
class SuiteSourceCurrentCellHighlighter {

    private static final String MARKER_ID = "org.robotframework.red.cellHighlighting";

    private final SuiteSourceEditor editor;

    private final IFile file;

    private final IDocument document;

    private IRegion currentCell;

    SuiteSourceCurrentCellHighlighter(final SuiteSourceEditor editor, final IFile file, final IDocument document) {
        this.editor = editor;
        this.file = file;
        this.document = document;
        this.currentCell = null;
    }

    void install(final SourceViewer viewer) {
        if (file == null) {
            return;
        }
        viewer.getTextWidget().addCaretListener(new CaretListener() {

            @Override
            public void caretMoved(final CaretEvent event) {
                refreshCurrentCell(event.caretOffset);
            }
        });
        viewer.getTextWidget().addDisposeListener(new DisposeListener() {

            @Override
            public void widgetDisposed(final DisposeEvent e) {
                refreshCurrentCell(-1);
            }
        });
    }

    private void refreshCurrentCell(final int offset) {
        try {
            final Optional<IRegion> newRegion = offset == -1 ? Optional.<IRegion> absent()
                    : DocumentUtilities.findCellRegion(document, isTsv(), offset);
            if (!newRegion.isPresent()) {
                removeCellHighlighting();
                currentCell = null;
                editor.notifyDocSelectionChangedListener(new Region(offset, 1), false);
            } else if (!Objects.equals(currentCell, newRegion.get())) {
                removeCellHighlighting();
                highlightCell(newRegion.get());
                currentCell = newRegion.get();
                editor.notifyDocSelectionChangedListener(currentCell, false);
            }
        } catch (final BadLocationException | InterruptedException e) {
            RedPlugin.logError("Unable to create cell highlight markers", e);
        }
    }

    private boolean isTsv() {
        return file.getFileExtension().equals("tsv");
    }

    private void removeCellHighlighting() {
        try {
            if (file.exists()) {
                file.deleteMarkers(MARKER_ID, true, IResource.DEPTH_ONE);
            }
        } catch (final CoreException e) {
            RedPlugin.logError("Unable to remove cell highlight markers", e);
        }
    }

    private void highlightCell(final IRegion newRegion) throws BadLocationException, InterruptedException {
        final String cellContent = document.get(newRegion.getOffset(), newRegion.getLength());
        final WorkspaceJob wsJob = new WorkspaceJob("Creating cell highlight marker") {

            @Override
            public IStatus runInWorkspace(final IProgressMonitor monitor) throws CoreException {
                createMarker(newRegion, cellContent);
                return Status.OK_STATUS;
            }
        };
        wsJob.setSystem(true);
        wsJob.schedule();
        wsJob.join();
    }

    private void createMarker(final IRegion region, final String selectedText) {
        try {
            if (file.exists()) {
                final IMarker marker = file.createMarker(MARKER_ID);
                marker.setAttribute(IMarker.TRANSIENT, true);
                marker.setAttribute(IMarker.CHAR_START, region.getOffset());
                marker.setAttribute(IMarker.CHAR_END, region.getOffset() + region.getLength());
            }
        } catch (final CoreException e) {
            RedPlugin.logError("Unable to create cell highlighting marker for '" + selectedText + "'", e);
        }
    }
}
