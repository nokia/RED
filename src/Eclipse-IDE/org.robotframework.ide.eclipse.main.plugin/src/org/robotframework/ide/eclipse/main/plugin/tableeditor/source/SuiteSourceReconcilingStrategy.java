/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.source;

import java.util.Collection;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.reconciler.DirtyRegion;
import org.eclipse.jface.text.reconciler.IReconcilingStrategy;
import org.eclipse.jface.text.reconciler.IReconcilingStrategyExtension;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.rf.ide.core.testdata.model.RobotFileOutput;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotArtifactsValidator;

public class SuiteSourceReconcilingStrategy implements IReconcilingStrategy, IReconcilingStrategyExtension {

    private RobotDocument document;

    private final SuiteSourceEditor editor;

    public SuiteSourceReconcilingStrategy(final SuiteSourceEditor editor) {
        this.editor = editor;
    }

    private RobotSuiteFile getSuiteModel() {
        return editor.getFileModel();
    }

    private SuiteSourceEditorFoldingSupport getFoldingSupport() {
        return editor.getFoldingSupport();
    }

    @Override
    public void setProgressMonitor(final IProgressMonitor monitor) {
        // we don't need monitor
    }

    @Override
    public void setDocument(final IDocument document) {
        this.document = (RobotDocument) document;
    }

    @Override
    public void initialReconcile() {
        getFoldingSupport().reset();
        updateFoldingStructure();
    }

    @Override
    public void reconcile(final DirtyRegion dirtyRegion, final IRegion subRegion) {
        reconcile();

        editor.notifyDocSelectionChangedListener(dirtyRegion, true);
    }

    @Override
    public void reconcile(final IRegion partition) {
        reconcile();
    }

    private void reconcile() {
        reparseModel();
        updateFoldingStructure();
        RobotArtifactsValidator.revalidate(getSuiteModel());
    }

    private void reparseModel() {
        final RobotFileOutput fileOutput = document.getNewestFileOutput();

        final RobotSuiteFile suiteModel = getSuiteModel();
        suiteModel.dispose();
        suiteModel.link(fileOutput);

        final IEventBroker eventBroker = (IEventBroker) PlatformUI.getWorkbench().getService(IEventBroker.class);
        eventBroker.post(RobotModelEvents.REPARSING_DONE, suiteModel);
    }

    private void updateFoldingStructure() {
        final Collection<Position> positions = getFoldingSupport().calculateFoldingPositions(getSuiteModel(), document);
        Display.getDefault().asyncExec(new Runnable() {

            @Override
            public void run() {
                getFoldingSupport().updateFoldingStructure(positions);
            }
        });
    }
}
