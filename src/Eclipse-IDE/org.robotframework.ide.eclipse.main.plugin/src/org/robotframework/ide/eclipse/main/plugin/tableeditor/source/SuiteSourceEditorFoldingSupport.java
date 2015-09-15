/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.source;

import static com.google.common.collect.Lists.newArrayList;

import java.util.HashMap;
import java.util.List;

import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.projection.ProjectionAnnotation;
import org.eclipse.jface.text.source.projection.ProjectionAnnotationModel;

class SuiteSourceEditorFoldingSupport {

    private final ProjectionAnnotationModel annotationsModel;

    private List<? extends Annotation> oldFoldingAnnotations;

    SuiteSourceEditorFoldingSupport(final ProjectionAnnotationModel annotationsModel) {
        this.annotationsModel = annotationsModel;
        this.oldFoldingAnnotations = newArrayList();
    }

    public void updateFoldingStructure(final List<Position> positions) {
        final HashMap<ProjectionAnnotation, Position> newAnnotations = new HashMap<>();
        for (final Position position : positions) {
            final ProjectionAnnotation annotation = new ProjectionAnnotation();
            newAnnotations.put(annotation, position);
        }

        final List<? extends Annotation> annotations = newArrayList(newAnnotations.keySet());

        annotationsModel.modifyAnnotations(oldFoldingAnnotations.toArray(new Annotation[0]), newAnnotations, null);
        oldFoldingAnnotations = annotations;
    }
}
