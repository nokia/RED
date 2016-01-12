/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.source;

import static com.google.common.collect.Iterables.transform;
import static com.google.common.collect.Lists.newArrayList;

import java.util.List;

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
import org.robotframework.ide.eclipse.main.plugin.model.RobotCasesSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCodeHoldingElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordsSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFileSection;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotArtifactsValidator;

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
        RobotArtifactsValidator.revalidate(getSuiteModel());
    }

    private void reparseModel() {
        final RobotSuiteFile suiteModel = getSuiteModel();
        suiteModel.reparseEverything(document.get());
        final IEventBroker eventBroker = (IEventBroker) PlatformUI.getWorkbench().getService(IEventBroker.class);
        eventBroker.post(RobotModelEvents.REPARSING_DONE, suiteModel);
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

        final int additionalLength = DocumentUtilities.getDelimiter(document).length();
        return newArrayList(transform(positions, delimiterShiftedPosition(additionalLength)));
    }

    private List<Position> calculateSectionsFoldingPositions(final RobotSuiteFile model) {
        final List<Position> positions = newArrayList();
        for (final RobotSuiteFileSection section : model.getChildren()) {
            positions.addAll(section.getPositions());
        }
        return positions;
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
            public Position apply(final RobotCodeHoldingElement element) {
                return element.getPosition();
            }
        };
    }

    private static Function<Position, Position> delimiterShiftedPosition(final int additionalLength) {
        return new Function<Position, Position>() {

            @Override
            public Position apply(final Position position) {
                return new Position(position.getOffset(), position.getLength() + additionalLength);
            }
        };
    }
}
