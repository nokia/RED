/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.views.documentation;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.IPageChangedListener;
import org.eclipse.jface.dialogs.PageChangedEvent;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.nebula.widgets.nattable.coordinate.PositionCoordinate;
import org.eclipse.swt.custom.CaretEvent;
import org.eclipse.swt.custom.CaretListener;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartSite;
import org.rf.ide.core.testdata.model.RobotFile;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotFormEditor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.SelectionLayerAccessor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.DocumentUtilities;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.RobotDocument;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.SuiteSourceEditor;
import org.robotframework.red.swt.SwtThread;

class DocumentationViewPartListener implements IPartListener {

    private RobotFormEditor currentlyActiveEditor;

    private EditorListener editorListener;

    @Override
    public void partActivated(final IWorkbenchPart part) {
        if (part.getSite().getId().equals(RobotFormEditor.ID)) {
            currentlyActiveEditor = (RobotFormEditor) part;
            editorListener = new EditorListener();
            registerEditorListener();

            Documentations.markViewSyncBroken(currentlyActiveEditor.getSite());

            if (currentlyActiveEditor.getActiveEditor() instanceof SuiteSourceEditor) {
                final int caretOffset = currentlyActiveEditor.getSourceEditor()
                        .getViewer()
                        .getTextWidget()
                        .getCaretOffset();
                editorListener.displayForSourceSelection(caretOffset);
            } else {
                final ISelection selection = currentlyActiveEditor.getEditorSite()
                        .getSelectionProvider()
                        .getSelection();
                editorListener.displayForTableSelection(selection);
            }
        }
    }

    private void registerEditorListener() {
        currentlyActiveEditor.addPageChangedListener(editorListener);
        currentlyActiveEditor.getEditorSite().getSelectionProvider().addSelectionChangedListener(editorListener);
        currentlyActiveEditor.getSourceEditor().getViewer().getTextWidget().addCaretListener(editorListener);
    }

    @Override
    public void partDeactivated(final IWorkbenchPart part) {
        if (currentlyActiveEditor == part) {
            removeEditorListener();
            editorListener = null;
            currentlyActiveEditor = null;
        }
    }

    private void removeEditorListener() {
        currentlyActiveEditor.removePageChangedListener(editorListener);
        currentlyActiveEditor.getEditorSite().getSelectionProvider().removeSelectionChangedListener(editorListener);
        currentlyActiveEditor.getSourceEditor().getViewer().getTextWidget().removeCaretListener(editorListener);
    }

    @Override
    public void partOpened(final IWorkbenchPart part) {
        // nothing to do
    }

    @Override
    public void partClosed(final IWorkbenchPart part) {
        // nothing to do
    }

    @Override
    public void partBroughtToTop(final IWorkbenchPart part) {
        // nothing to do
    }

    void dispose() {
        if (currentlyActiveEditor != null) {
            removeEditorListener();
        }
    }

    private class EditorListener implements ISelectionChangedListener, CaretListener, IPageChangedListener {

        private SelectionInput selectionInput;

        private Job changeInputJob;

        @Override
        public void pageChanged(final PageChangedEvent event) {
            // when switching from table page to source page caretMoved event is not generated, so we
            // display source selection manually; on the other hand when switching from source to table
            // selection changed is generated, so the listener will be notified
            final Object selectedPage = event.getSelectedPage();
            if (selectedPage instanceof SuiteSourceEditor) {
                final int caretOffset = ((SuiteSourceEditor) selectedPage).getViewer().getTextWidget().getCaretOffset();
                displayForSourceSelection(caretOffset);
            }
        }

        @Override
        public void selectionChanged(final SelectionChangedEvent event) {
            final ISelection selection = event.getSelection();
            displayForTableSelection(selection);
        }

        private void displayForTableSelection(final ISelection selection) {
            if (selection instanceof IStructuredSelection) {
                final IWorkbenchPartSite site = currentlyActiveEditor.getSite();
                Documentations.markViewSyncBroken(site);

                final SelectionLayerAccessor selectionLayerAccessor = currentlyActiveEditor.getSelectionLayerAccessor();

                // selection could change between different tables, so we add table id
                final Optional<TableSelectedCellInput> newInput = Stream
                        .of(selectionLayerAccessor.getSelectedPositions())
                        .findFirst()
                        .map(pos -> new TableSelectedCellInput(selectionLayerAccessor.hashCode(), pos));

                if (newInput.isPresent() && !Objects.equals(selectionInput, newInput.get())) {
                    selectionInput = newInput.get();
                    scheduleShowDocJob(() -> {},
                            () -> Documentations.showDocForEditorTablesSelection(site, selectionLayerAccessor));
                }
            }
        }

        @Override
        public void caretMoved(final CaretEvent event) {
            final int offset = event.caretOffset;
            displayForSourceSelection(offset);
        }

        private void displayForSourceSelection(final int offset) {
            final IWorkbenchPartSite site = currentlyActiveEditor.getSite();
            Documentations.markViewSyncBroken(site);

            final SuiteSourceEditor sourceEditor = currentlyActiveEditor.getSourceEditor();
            final RobotSuiteFile fileModel = currentlyActiveEditor.provideSuiteModel();
            final IDocument document = sourceEditor.getDocument();
            try {
                final Optional<SelectionInput> newInput = DocumentUtilities
                        .findCellRegion(document, fileModel.isTsvFile(), offset)
                        .map(SourceSelectedCellInput::new);

                if (newInput.isPresent() && !Objects.equals(selectionInput, newInput.get())) {
                    // we need the job to wait until reconciler will reparse the file and link new
                    // output to suite model
                    selectionInput = newInput.get();
                    scheduleShowDocJob(() -> {
                        try {
                            final RobotFile newestModel = ((RobotDocument) document).getNewestModel();
                            RobotFile currentModel = fileModel.getLinkedElement();
                            final long watingStart = System.currentTimeMillis();

                            // once reconciler gets new model from document, those objects should be
                            // the same
                            while (currentModel != newestModel) {
                                Thread.sleep(300);
                                currentModel = fileModel.getLinkedElement();

                                // to avoid infinite loops
                                final long waitingEnd = System.currentTimeMillis();
                                if (waitingEnd - watingStart > 5000) {
                                    throw new IllegalStateException();
                                }
                            }
                        } catch (final InterruptedException e) {
                            // ok, out of sync in that case
                        }
                    }, () -> Documentations.showDocForEditorSourceSelection(site, fileModel, document, offset));

                }
            } catch (final BadLocationException e) {
                // ignore this
            }
        }

        private void scheduleShowDocJob(final Runnable await, final Runnable docDisplayer) {
            if (changeInputJob != null && changeInputJob.getState() == Job.SLEEPING) {
                changeInputJob.cancel();
            }
            changeInputJob = Job.create("", monitor -> {
                try {
                    await.run();
                } catch (final IllegalStateException e) {
                    // waiting for reparse goes wrong, so we'll try to display something probably
                    // not up-to-date
                } finally {
                    SwtThread.asyncExec(docDisplayer);
                }
            });
            changeInputJob.schedule(400);
        }
    }

    private static interface SelectionInput { }

    private static final class SourceSelectedCellInput implements SelectionInput {

        private final IRegion cellRegion;

        public SourceSelectedCellInput(final IRegion cellRegion) {
            this.cellRegion = cellRegion;
        }

        @Override
        public boolean equals(final Object obj) {
            if (obj != null && obj.getClass() == SourceSelectedCellInput.class) {
                final SourceSelectedCellInput that = (SourceSelectedCellInput) obj;
                return this.cellRegion.equals(that.cellRegion);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return cellRegion.hashCode();
        }
    }

    private static final class TableSelectedCellInput implements SelectionInput {

        private final int tableId;
        private final PositionCoordinate tableCellPosition;

        public TableSelectedCellInput(final int tableId, final PositionCoordinate tableCellPosition) {
            this.tableId = tableId;
            this.tableCellPosition = tableCellPosition;
        }

        @Override
        public boolean equals(final Object obj) {
            if (obj != null && obj.getClass() == TableSelectedCellInput.class) {
                final TableSelectedCellInput that = (TableSelectedCellInput) obj;
                return this.tableId == that.tableId && this.tableCellPosition.equals(that.tableCellPosition);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return Objects.hash(tableId, tableCellPosition);
        }
    }
}
