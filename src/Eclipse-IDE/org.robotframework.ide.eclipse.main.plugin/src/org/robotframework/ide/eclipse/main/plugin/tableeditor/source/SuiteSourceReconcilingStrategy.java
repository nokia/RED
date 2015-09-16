/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.source;

import static com.google.common.collect.Lists.newArrayList;

import java.util.Collection;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.reconciler.DirtyRegion;
import org.eclipse.jface.text.reconciler.IReconcilingStrategy;
import org.eclipse.jface.text.reconciler.IReconcilingStrategyExtension;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCasesSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCodeHoldingElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordsSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotArtifactsValidator;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotArtifactsValidator.ModelUnitValidator;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.validation.RobotFileValidator;
import org.robotframework.ide.eclipse.main.plugin.project.build.validation.ValidationContext;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;

public class SuiteSourceReconcilingStrategy implements IReconcilingStrategy, IReconcilingStrategyExtension {

    private IDocument document;

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
        this.document = document;
    }

    @Override
    public void initialReconcile() {
        updateFoldingStructure();
    }

    @Override
    public void reconcile(final DirtyRegion dirtyRegion, final IRegion subRegion) {
        reconcile();
    }

    @Override
    public void reconcile(final IRegion partition) {
        reconcile();
    }

    private void reconcile() {
        reparseModel();
        updateFoldingStructure();
        revalidate();
    }

    private void reparseModel() {
        final RobotSuiteFile suiteModel = getSuiteModel();
        suiteModel.reparseEverything(document.get());
        PlatformUI.getWorkbench().getService(IEventBroker.class).post(RobotModelEvents.RECONCILATION_DONE, suiteModel);
    }

    private void updateFoldingStructure() {
        final List<Position> positions = calculateFoldingPositions(getSuiteModel());
        Display.getDefault().asyncExec(new Runnable() {
            @Override
            public void run() {
                getFoldingSupport().updateFoldingStructure(positions);
            }
        });
    }

    private List<Position> calculateFoldingPositions(final RobotSuiteFile model) {
        final List<Position> positions = newArrayList();

        positions.addAll(calculateSectionsFoldingPositions(model));
        positions.addAll(calculateTestCasesFoldingPositions(model));
        positions.addAll(calculateKeywordsFoldingPositions(model));

        return positions;
    }

    private Collection<? extends Position> calculateSectionsFoldingPositions(final RobotSuiteFile model) {
        // TODO implement this, sections should be foldable too
        return newArrayList();
    }

    private List<Position> calculateTestCasesFoldingPositions(final RobotSuiteFile model) {
        final Optional<RobotCasesSection> casesSection = model.findSection(RobotCasesSection.class);
        if (casesSection.isPresent()) {
            return Lists.transform(casesSection.get().getChildren(), toPositions());
        }
        return Lists.<Position> newArrayList();
    }

    private List<Position> calculateKeywordsFoldingPositions(final RobotSuiteFile model) {
        final Optional<RobotKeywordsSection> keywordSection = model.findSection(RobotKeywordsSection.class);
        if (keywordSection.isPresent()) {
            return Lists.transform(keywordSection.get().getChildren(), toPositions());
        }
        return Lists.<Position> newArrayList();
    }

    private static Function<RobotCodeHoldingElement, Position> toPositions() {
        return new Function<RobotCodeHoldingElement, Position>() {

            @Override
            public Position apply(final RobotCodeHoldingElement testCase) {
                return testCase.getPosition();
            }
        };
    }

    private void revalidate() {
        final IFile file = getSuiteModel().getFile();
        final Optional<? extends ModelUnitValidator> validator = RobotArtifactsValidator
                .createProperValidator(prepareValidationContext(), file);

        if (validator.isPresent()) {
            final WorkspaceJob wsJob = new WorkspaceJob("Revalidating model") {

                @Override
                public IStatus runInWorkspace(final IProgressMonitor monitor) throws CoreException {
                    file.deleteMarkers(RobotProblem.TYPE_ID, true, 1);
                    ((RobotFileValidator) validator.get()).validate(getSuiteModel().getLinkedElement(),
                            new NullProgressMonitor());

                    return Status.OK_STATUS;
                }
            };
            wsJob.setSystem(true);
            wsJob.schedule();
        }
    }

    private ValidationContext prepareValidationContext() {
        return new ValidationContext(getSuiteModel().getProject().getRuntimeEnvironment());
    }
}
