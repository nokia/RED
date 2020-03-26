/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.red.viewers;


import java.lang.reflect.Array;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;

import com.google.common.base.Predicates;

public class Selections {

    public static final String SELECTION = "selection";

    @SuppressWarnings("unchecked")
    public static <T> T[] getElementsArray(final IStructuredSelection selection, final Class<T> elementsClass) {
        return getElementsStream(selection, elementsClass).toArray(l -> (T[]) Array.newInstance(elementsClass, l));
    }

    public static <T> List<T> getElements(final IStructuredSelection selection, final Class<T> elementsClass) {
        return getElementsStream(selection, elementsClass).collect(Collectors.toList());
    }

    public static <T> Stream<T> getElementsStream(final IStructuredSelection selection, final Class<T> elementsClass) {
        final List<?> elements = selection.toList();
        return elements.stream().filter(elementsClass::isInstance).map(elementsClass::cast);
    }

    public static <T> List<T> getAdaptableElements(final IStructuredSelection selection, final Class<T> elementsClass) {
        final List<?> elements = selection.toList();
        return elements
                .stream()
                .map(obj -> RedPlugin.getAdapter(obj, elementsClass))
                .filter(Predicates.notNull())
                .collect(Collectors.toList());
    }

    public static <T> T getSingleElement(final IStructuredSelection selection, final Class<T> elementsClass) {
        return getElementsStream(selection, elementsClass).limit(2)
                .collect(Collectors.collectingAndThen(Collectors.toList(), elements -> {
                    if (elements.size() == 1) {
                        return elements.get(0);
                    }
                    throw new IllegalArgumentException("Given selection should contain only one element of class "
                            + elementsClass.getName() + ", but have " + elements.size() + " elements instead");
                }));
    }

    public static <T> Optional<T> getOptionalFirstElement(final IStructuredSelection selection,
            final Class<T> elementsClass) {
        return getElementsStream(selection, elementsClass).findFirst();
    }

    @SuppressWarnings("unchecked")
    public static Optional<Object> getOptionalFirstElement(final IStructuredSelection selection,
            final Predicate<Object> predicate) {
        final List<Object> elements = selection.toList();
        return elements.stream().filter(predicate).findFirst();
    }
}
