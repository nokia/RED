/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.source;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.eclipse.jface.text.BadLocationException;
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

/**
 * @author Michal Anglart
 */
class SuiteSourceCurrentCellHighlighter {

    private static final String ANNOTATION_ID = "org.robotframework.red.cellHighlighting";

    private final RobotSuiteFile fileModel;

    private final IDocument document;

    private IRegion currentCell;

    private IAnnotationModel annotationModel;

    SuiteSourceCurrentCellHighlighter(final RobotSuiteFile fileModel, final IDocument document) {
        this.fileModel = fileModel;
        this.document = document;
        this.currentCell = null;
    }

    void install(final SourceViewer viewer) {
        annotationModel = viewer.getAnnotationModel();
        viewer.getTextWidget().addCaretListener(new CaretListener() {

            @Override
            public void caretMoved(final CaretEvent event) {
                refreshCurrentCell(event.caretOffset);
            }
        });
    }

    @VisibleForTesting
    void refreshCurrentCell(final int offset) {
        try {
            final Optional<IRegion> newRegion = DocumentUtilities.findCellRegion(document, fileModel.isTsvFile(),
                    offset);

            final Annotation[] annotationsToRemove;
            final Map<Annotation, Position> annotationsToAdd;
            if (!newRegion.isPresent()) {

                annotationsToRemove = getAnnotationsToRemove();
                annotationsToAdd = new HashMap<>();
                currentCell = null;

                ((IAnnotationModelExtension) annotationModel).replaceAnnotations(annotationsToRemove, annotationsToAdd);

            } else if (!Objects.equals(currentCell, newRegion.get())) {

                annotationsToRemove = getAnnotationsToRemove();
                annotationsToAdd = getAnnotationsToAdd(newRegion.get());
                currentCell = newRegion.get();

                ((IAnnotationModelExtension) annotationModel).replaceAnnotations(annotationsToRemove, annotationsToAdd);
            }
        } catch (final BadLocationException e) {
            // silently ignore this
        }
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

    private Map<Annotation, Position> getAnnotationsToAdd(final IRegion region) {
        final Map<Annotation, Position> annotationsToAdd = new HashMap<>();
        annotationsToAdd.put(new Annotation(ANNOTATION_ID, false, ""),
                new Position(region.getOffset(), region.getLength()));
        return annotationsToAdd;
    }
}
