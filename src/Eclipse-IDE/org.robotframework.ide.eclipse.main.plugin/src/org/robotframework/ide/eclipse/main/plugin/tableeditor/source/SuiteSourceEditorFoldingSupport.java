/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.source;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.projection.ProjectionAnnotation;
import org.eclipse.jface.text.source.projection.ProjectionAnnotationModel;
import org.eclipse.swt.custom.StyledText;

import com.google.common.annotations.VisibleForTesting;

class SuiteSourceEditorFoldingSupport {

    private final StyledText textControl;
    private final ProjectionAnnotationModel annotationsModel;

    private Map<Position, Annotation> oldFoldingAnnotations;

    SuiteSourceEditorFoldingSupport(final StyledText textControl, final ProjectionAnnotationModel annotationsModel) {
        this.textControl = textControl;
        this.annotationsModel = annotationsModel;
        this.oldFoldingAnnotations = new HashMap<>();
    }

    @VisibleForTesting
    Map<Position, Annotation> getOldAnnotations() {
        return oldFoldingAnnotations;
    }

    public void updateFoldingStructure(final List<Position> positions) {
        if (annotationsModel == null) {
            return;
        }

        final List<Annotation> annotationsToRemove = new ArrayList<>();
        final HashMap<ProjectionAnnotation, Position> annotationsToAdd = new HashMap<>();
        final List<Annotation> annotationsToChange = new ArrayList<>();

        final Map<Position, Annotation> newFoldingAnnotations = new HashMap<>();
        for (final Position position : positions) {
            if (oldFoldingAnnotations.containsKey(position)) {
                final Annotation annotation = oldFoldingAnnotations.get(position);
                annotationsToChange.add(annotation);
                newFoldingAnnotations.put(position, annotation);

            } else {
                final ProjectionAnnotation annotation = new ProjectionAnnotation();
                annotationsToAdd.put(annotation, position);
                newFoldingAnnotations.put(position, annotation);
            }
        }
        for (final Position position : oldFoldingAnnotations.keySet()) {
            if (!newFoldingAnnotations.containsKey(position)) {
                annotationsToRemove.add(oldFoldingAnnotations.get(position));
            }
        }

        try {
            textControl.setRedraw(false);
            annotationsModel.modifyAnnotations(annotationsToRemove.toArray(new Annotation[0]), annotationsToAdd,
                    annotationsToChange.toArray(new Annotation[0]));
            // workaround : without this the horizontal scrollbar is reset to 0 position when
            // writing at the end of long line
            textControl.showSelection();
        } finally {
            textControl.setRedraw(true);
            oldFoldingAnnotations = newFoldingAnnotations;
        }

    }
}
