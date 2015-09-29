/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.source;

import static com.google.common.collect.Lists.newArrayList;

import java.util.Iterator;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.quickassist.IQuickAssistInvocationContext;
import org.eclipse.jface.text.quickassist.IQuickAssistProcessor;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.ui.texteditor.MarkerAnnotation;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.IProblemCause;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.RedMarkerResolution;
import org.robotframework.ide.eclipse.main.plugin.project.build.fix.ProjectsFixesGenerator;

import com.google.common.base.Function;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.google.common.collect.Range;

/**
 * @author Michal Anglart
 *
 */
public class SuiteSourceQuickAssistProcessor implements IQuickAssistProcessor {

    private final RobotSuiteFile suiteModel;

    public SuiteSourceQuickAssistProcessor(final RobotSuiteFile fileModel) {
        this.suiteModel = fileModel;
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
                return RobotProblem.TYPE_ID.equals(marker.getType())
                        && ProjectsFixesGenerator.getCause(marker).hasResolution();
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
                        final IProblemCause cause = ProjectsFixesGenerator.getCause(marker);
                        if (cause.hasResolution()) {
                            final Position position = getPosition((MarkerAnnotation) annotation);
                            if (position != null
                                    && Range.closed(position.getOffset(), position.getOffset() + position.getLength())
                                            .contains(invocationContext.getOffset())) {
                                final Iterable<RedMarkerResolution> fixers = Iterables
                                        .filter(cause.createFixers(marker), RedMarkerResolution.class);
                                return newArrayList(Iterables.filter(Iterables.transform(fixers,
                                        new Function<RedMarkerResolution, ICompletionProposal>() {

                                            @Override
                                            public ICompletionProposal apply(final RedMarkerResolution resolution) {
                                                return resolution.asContentProposal(marker,
                                                        invocationContext.getSourceViewer().getDocument(), suiteModel)
                                                        .orNull();
                                            }
                                        }), Predicates.notNull())).toArray(new ICompletionProposal[0]);
                            }
                        }
                    }
                } catch (final CoreException e) {
                    return null;
                }
            }
        }
        return null;
    }
}
