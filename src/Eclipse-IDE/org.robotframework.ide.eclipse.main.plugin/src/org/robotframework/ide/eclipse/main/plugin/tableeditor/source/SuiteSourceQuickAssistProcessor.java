/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.source;

import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.contentassist.ContentAssistEvent;
import org.eclipse.jface.text.contentassist.ICompletionListener;
import org.eclipse.jface.text.contentassist.ICompletionListenerExtension;
import org.eclipse.jface.text.contentassist.ICompletionListenerExtension2;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.quickassist.IQuickAssistInvocationContext;
import org.eclipse.jface.text.quickassist.IQuickAssistProcessor;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IMarkerResolution;
import org.eclipse.ui.texteditor.MarkerAnnotation;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.IProblemCause;
import org.robotframework.ide.eclipse.main.plugin.project.build.fix.ProjectsFixesGenerator;
import org.robotframework.ide.eclipse.main.plugin.project.build.fix.RedSuiteMarkerResolution;
import org.robotframework.ide.eclipse.main.plugin.project.build.fix.RedXmlConfigMarkerResolution;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.assist.RedCompletionProposal;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.assist.RedCompletionProposalAdapter;

import com.google.common.base.Predicates;
import com.google.common.collect.Range;

/**
 * @author Michal Anglart
 */
public class SuiteSourceQuickAssistProcessor implements IQuickAssistProcessor, ICompletionListener,
        ICompletionListenerExtension, ICompletionListenerExtension2 {

    private final RobotSuiteFile suiteModel;

    private final SourceViewer sourceViewer;

    public SuiteSourceQuickAssistProcessor(final RobotSuiteFile fileModel, final ISourceViewer sourceViewer) {
        this.suiteModel = fileModel;
        this.sourceViewer = (SourceViewer) sourceViewer;
    }

    @Override
    public String getErrorMessage() {
        return null;
    }

    @Override
    public boolean canFix(final Annotation annotation) {
        if (annotation instanceof MarkerAnnotation) {
            try {
                final IMarker marker = ((MarkerAnnotation) annotation).getMarker();
                if (RobotProblem.TYPE_ID.equals(marker.getType())) {
                    final IProblemCause cause = ProjectsFixesGenerator.getCause(marker);
                    return cause != null && cause.hasResolution();
                }
            } catch (final CoreException e) {
                return false;
            }
        }
        return false;
    }

    @Override
    public boolean canAssist(final IQuickAssistInvocationContext invocationContext) {
        return false;
    }

    private static Position getPosition(final MarkerAnnotation annotation) {
        try {
            final Integer start = (Integer) annotation.getMarker().getAttribute(IMarker.CHAR_START);
            final Integer end = (Integer) annotation.getMarker().getAttribute(IMarker.CHAR_END);
            return new Position(start, end - start);
        } catch (final CoreException e) {
            return null;
        }
    }

    @Override
    public ICompletionProposal[] computeQuickAssistProposals(final IQuickAssistInvocationContext invocationContext) {

        final Iterator<?> annotations = invocationContext.getSourceViewer()
                .getAnnotationModel()
                .getAnnotationIterator();

        while (annotations.hasNext()) {
            final Annotation annotation = (Annotation) annotations.next();
            if (annotation instanceof MarkerAnnotation) {
                final IMarker marker = ((MarkerAnnotation) annotation).getMarker();
                try {
                    if (RobotProblem.TYPE_ID.equals(marker.getType())) {
                        final Position position = getPosition((MarkerAnnotation) annotation);
                        final IProblemCause cause = ProjectsFixesGenerator.getCause(marker);

                        if (cause.hasResolution() && isInvokedWithinAnnotationPosition(invocationContext, position)) {
                            final List<ICompletionProposal> proposals = computeRobotProblemsAssistants(
                                    invocationContext, marker, cause);
                            return proposals.isEmpty() ? null : proposals.toArray(new ICompletionProposal[0]);
                        }
                    }
                } catch (final CoreException e) {
                    return null;
                }
            }
        }
        return null;
    }

    private List<ICompletionProposal> computeRobotProblemsAssistants(
            final IQuickAssistInvocationContext invocationContext, final IMarker marker, final IProblemCause cause) {

        final List<? extends IMarkerResolution> fixers = cause.createFixers(marker);
        final Stream<ICompletionProposal> redXmlRepairProposals = fixers.stream()
                .filter(RedXmlConfigMarkerResolution.class::isInstance)
                .map(RedXmlConfigMarkerResolution.class::cast)
                .map(resolution -> resolution.asContentProposal(marker));
        final Stream<ICompletionProposal> suiteRepairProposals = fixers.stream()
                .filter(RedSuiteMarkerResolution.class::isInstance)
                .map(RedSuiteMarkerResolution.class::cast)
                .map(resolution -> resolution
                        .asContentProposal(marker, invocationContext.getSourceViewer().getDocument(), suiteModel)
                        .orElse(null));
        return Stream.concat(redXmlRepairProposals, suiteRepairProposals).filter(Predicates.notNull()).collect(
                Collectors.toList());
    }

    private boolean isInvokedWithinAnnotationPosition(final IQuickAssistInvocationContext invocationContext,
            final Position annotationPosition) {
        return annotationPosition != null && Range
                .closed(annotationPosition.getOffset(), annotationPosition.getOffset() + annotationPosition.getLength())
                .contains(invocationContext.getOffset());
    }

    @Override
    public void applied(final ICompletionProposal proposal) {
        // this method is called also for processors from which the proposal was not chosen
        // hence canReopenAssistantProgramatically is holding information which processor
        // is able to open proposals after accepting
        if (shouldActivateAssist(proposal)) {
            Display.getCurrent().asyncExec(() -> sourceViewer.doOperation(ISourceViewer.CONTENTASSIST_PROPOSALS));
        }
    }

    private boolean shouldActivateAssist(final ICompletionProposal proposal) {
        return proposal instanceof RedCompletionProposal
                && ((RedCompletionProposal) proposal).shouldActivateAssistantAfterAccepting()
                || proposal instanceof RedCompletionProposalAdapter
                        && ((RedCompletionProposalAdapter) proposal).shouldActivateAssistantAfterAccepting();
    }

    @Override
    public void assistSessionEnded(final ContentAssistEvent event) {
        // nothing to do
    }

    @Override
    public void assistSessionStarted(final ContentAssistEvent event) {
        // nothing to do
    }

    @Override
    public void assistSessionRestarted(final ContentAssistEvent event) {
        // nothing to do
    }

    @Override
    public void selectionChanged(final ICompletionProposal proposal, final boolean smartToggle) {
        // nothing to do
    }
}
