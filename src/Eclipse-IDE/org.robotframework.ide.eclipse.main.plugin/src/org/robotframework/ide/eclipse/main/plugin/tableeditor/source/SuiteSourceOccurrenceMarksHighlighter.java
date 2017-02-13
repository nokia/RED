/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.source;

import static com.google.common.collect.Sets.newHashSet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.FindReplaceDocumentAdapter;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.IAnnotationModelExtension;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.swt.custom.CaretEvent;
import org.eclipse.swt.custom.CaretListener;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Optional;

class SuiteSourceOccurrenceMarksHighlighter {

    private static final String ANNOTATION_ID = "org.robotframework.ide.texteditor.occurrencesMark";

    private final RobotSuiteFile fileModel;

    private final IDocument document;

    private final FindReplaceDocumentAdapter findAdapter;

    private Set<IRegion> occurrencesRegions;

    private Job refreshingJob;

    private IAnnotationModel annotationModel;

    SuiteSourceOccurrenceMarksHighlighter(final RobotSuiteFile fileModel, final IDocument document) {
        this.document = document;
        this.fileModel = fileModel;
        this.findAdapter = new FindReplaceDocumentAdapter(document);
        this.occurrencesRegions = newHashSet();
        this.refreshingJob = null;
    }

    void install(final SourceViewer viewer) {
        annotationModel = viewer.getAnnotationModel();
        viewer.getTextWidget().addCaretListener(new CaretListener() {
            @Override
            public void caretMoved(final CaretEvent event) {
                scheduleRefresh(event.caretOffset);
            }
        });
    }

    private void scheduleRefresh(final int offset) {
        if (refreshingJob != null && refreshingJob.getState() == Job.SLEEPING) {
            refreshingJob.cancel();
        }
        refreshingJob = new Job("") {

            @Override
            protected IStatus run(final IProgressMonitor monitor) {
                refreshOccurrences(offset);
                return Status.OK_STATUS;
            }
        };
        refreshingJob.setSystem(true);
        refreshingJob.schedule(50);
    }

    @VisibleForTesting
    void refreshOccurrences(final int offset) {
        try {
            final Optional<IRegion> currentRegion = getCurrentRegion(offset);

            final Annotation[] annotationsToRemove;
            final Map<Annotation, Position> annotationsToAdd;
            if (!currentRegion.isPresent()) {
                annotationsToRemove = getAnnotationsToRemove();
                annotationsToAdd = new HashMap<>();
                occurrencesRegions = newHashSet();

                ((IAnnotationModelExtension) annotationModel).replaceAnnotations(annotationsToRemove, annotationsToAdd);
            } else {
                final Set<IRegion> regions = findOccurrencesRegions(currentRegion.get());
                if (!Objects.equals(occurrencesRegions, regions)) {
                    annotationsToRemove = getAnnotationsToRemove();
                    annotationsToAdd = getAnnotationsToAdd(regions);
                    occurrencesRegions = regions;

                    ((IAnnotationModelExtension) annotationModel).replaceAnnotations(annotationsToRemove,
                            annotationsToAdd);
                }
            }
        } catch (final BadLocationException e) {
            // silently ignore this
        }
    }

    private Optional<IRegion> getCurrentRegion(final int offset) throws BadLocationException {
        final boolean isTsv = fileModel.isTsvFile();
        return DocumentUtilities.findVariable(document, isTsv, offset)
                .or(DocumentUtilities.findCellRegion(document, isTsv, offset));
    }

    private Set<IRegion> findOccurrencesRegions(final IRegion region) throws BadLocationException {
        final Set<IRegion> regions = newHashSet();

        final String selectedText = document.get(region.getOffset(), region.getLength()).trim();
        if (selectedText.isEmpty()) {
            return regions;
        }

        int currentOffset = 0;
        IRegion foundedRegion = findAdapter.find(currentOffset, selectedText, true, true, !isVariable(selectedText),
                false);
        while (foundedRegion != null) {
            regions.add(foundedRegion);

            currentOffset = foundedRegion.getOffset() + foundedRegion.getLength();
            foundedRegion = findAdapter.find(currentOffset, selectedText, true, true, !isVariable(selectedText), false);
        }

        return regions;
    }

    private static boolean isVariable(final String text) {
        return Pattern.matches("[@$&%]\\{.+\\}", text);
    }

    private Annotation[] getAnnotationsToRemove() {
        final List<Annotation> annotationsToRemove = new ArrayList<>();
        final Iterator<Annotation> annotationsIterator = annotationModel.getAnnotationIterator();
        while (annotationsIterator.hasNext()) {
            final Annotation annotation = annotationsIterator.next();
            if (ANNOTATION_ID.equals(annotation.getType())) {
                annotationsToRemove.add(annotation);
            }
        }
        return annotationsToRemove.toArray(new Annotation[0]);
    }

    private Map<Annotation, Position> getAnnotationsToAdd(final Set<IRegion> regions) {
        final Map<Annotation, Position> annotationsToAdd = new HashMap<>();
        for (final IRegion region : regions) {
            final Annotation annotation = new Annotation(ANNOTATION_ID, false, "");
            annotationsToAdd.put(annotation, new Position(region.getOffset(), region.getLength()));
        }
        return annotationsToAdd;
    }
}
