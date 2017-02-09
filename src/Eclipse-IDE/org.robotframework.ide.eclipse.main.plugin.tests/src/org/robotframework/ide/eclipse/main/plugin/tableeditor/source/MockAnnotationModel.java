/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.source;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.IAnnotationModelExtension;
import org.eclipse.jface.text.source.IAnnotationModelListener;


public class MockAnnotationModel implements IAnnotationModel, IAnnotationModelExtension {

    private final Map<Annotation, Position> annotations = new LinkedHashMap<>();

    @Override
    public void addAnnotationModel(final Object key, final IAnnotationModel attachment) {
        // nothing to do
    }

    @Override
    public IAnnotationModel getAnnotationModel(final Object key) {
        // nothing to do
        return null;
    }

    @Override
    public IAnnotationModel removeAnnotationModel(final Object key) {
        // nothing to do
        return null;
    }

    @Override
    public void replaceAnnotations(final Annotation[] annotationsToRemove,
            final Map<? extends Annotation, ? extends Position> annotationsToAdd) throws ClassCastException {
        for (final Annotation annotation : annotationsToRemove) {
            annotations.remove(annotation);
        }
        annotations.putAll(annotationsToAdd);
    }

    @Override
    public void modifyAnnotationPosition(final Annotation annotation, final Position position) {
        annotations.put(annotation, position);
    }

    @Override
    public void removeAllAnnotations() {
        annotations.clear();
    }

    @Override
    public Object getModificationStamp() {
        return null;
    }

    @Override
    public void addAnnotationModelListener(final IAnnotationModelListener listener) {
        // nothing to do
    }

    @Override
    public void removeAnnotationModelListener(final IAnnotationModelListener listener) {
        // nothing to do
    }

    @Override
    public void connect(final IDocument document) {
        // nothing to do
    }

    @Override
    public void disconnect(final IDocument document) {
        // nothing to do
    }

    @Override
    public void addAnnotation(final Annotation annotation, final Position position) {
        annotations.put(annotation, position);
    }

    @Override
    public void removeAnnotation(final Annotation annotation) {
        annotations.remove(annotation);
    }

    @Override
    public Iterator<Annotation> getAnnotationIterator() {
        return annotations.keySet().iterator();
    }

    @Override
    public Position getPosition(final Annotation annotation) {
        return annotations.get(annotation);
    }

    public Collection<Position> getPositions() {
        return annotations.values();
    }

    public boolean isEmpty() {
        return annotations.isEmpty();
    }
}
