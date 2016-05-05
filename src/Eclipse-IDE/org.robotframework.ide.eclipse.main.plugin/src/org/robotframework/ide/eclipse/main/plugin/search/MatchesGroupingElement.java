/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.search;

import java.util.Arrays;

import com.google.common.base.Optional;

/**
 * @author Michal Anglart
 *
 */
public final class MatchesGroupingElement {

    private final Object[] groupingElements;

    public MatchesGroupingElement(final Object... groupingElements) {
        this.groupingElements = groupingElements;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj != null && obj.getClass() == MatchesGroupingElement.class) {
            final MatchesGroupingElement that = (MatchesGroupingElement) obj;
            return Arrays.deepEquals(this.groupingElements, that.groupingElements);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(groupingElements);
    }

    <T> Optional<T> getGroupingObjectOf(final Class<? extends T> clazz) {
        for (final Object element : groupingElements) {
            if (clazz.isInstance(element)) {
                return Optional.<T> of(clazz.cast(element));
            }
        }
        return Optional.absent();
    }

}
