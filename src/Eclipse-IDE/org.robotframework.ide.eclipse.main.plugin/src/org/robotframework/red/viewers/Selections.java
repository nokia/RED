/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.red.viewers;

import static com.google.common.collect.Lists.newArrayList;

import java.lang.reflect.Array;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;

import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;

public class Selections {

    public static final String SELECTION = "selection";

    @SuppressWarnings("unchecked")
    public static <T> T[] getElementsArray(final IStructuredSelection selection, final Class<T> elementsClass) {
        final List<?> selectionAsList = selection.toList();
        return newArrayList(Iterables.filter(selectionAsList, elementsClass))
                .toArray((T[]) Array.newInstance(elementsClass, 0));
    }

    public static <T> List<T> getElements(final IStructuredSelection selection, final Class<T> elementsClass) {
        return newArrayList(Iterables.filter(selection.toList(), elementsClass));
    }

    public static <T> List<T> getAdaptableElements(final IStructuredSelection selection, final Class<T> elementsClass) {
        final List<?> selectionAsList = selection.toList();
        return selectionAsList.stream()
                .map(obj -> RedPlugin.getAdapter(obj, elementsClass))
                .filter(Predicates.notNull())
                .collect(Collectors.toList());
    }

    public static <T> T getSingleElement(final IStructuredSelection selection, final Class<T> elementsClass) {
        final List<T> elements = getElements(selection, elementsClass);
        if (elements.size() == 1) {
            return elements.get(0);
        }
        throw new IllegalArgumentException("Given selection should contain only one element of class "
                + elementsClass.getName() + ", but have " + elements.size() + " elements instead");
    }

    public static <T> Optional<T> getOptionalFirstElement(final IStructuredSelection selection,
            final Class<T> elementsClass) {
        return getElements(selection, elementsClass).stream().findFirst();
    }
}
