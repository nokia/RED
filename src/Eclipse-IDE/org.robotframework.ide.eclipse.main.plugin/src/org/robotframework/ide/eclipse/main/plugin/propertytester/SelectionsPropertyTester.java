/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.propertytester;

import java.util.List;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.robotframework.red.viewers.Selections;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;

public class SelectionsPropertyTester extends PropertyTester {

    @VisibleForTesting static final String ALL_ELEMENTS_HAVE_SAME_TYPE = "allElementsHaveSameType";

    @Override
    public boolean test(final Object receiver, final String property, final Object[] args, final Object expectedValue) {
        Preconditions.checkArgument(receiver instanceof IStructuredSelection,
                "Property tester is unable to test properties of " + receiver.getClass().getName()
                        + ". It should be used with " + IStructuredSelection.class.getName());

        if (expectedValue instanceof Boolean) {
            return testProperty((IStructuredSelection) receiver, property, ((Boolean) expectedValue).booleanValue());
        }
        return false;
    }

    private boolean testProperty(final IStructuredSelection selection, final String property, final boolean expected) {
        if (ALL_ELEMENTS_HAVE_SAME_TYPE.equals(property)) {
            final List<Object> elements = Selections.getElements(selection, Object.class);
            if (elements.isEmpty()) {
                return expected;
            }
            final Class<? extends Object> classOfFirst = elements.get(0).getClass();
            for (final Object element : elements) {
                if (!classOfFirst.isInstance(element)) {
                    return !expected;
                }
            }
            return expected;
        }
        return false;
    }
}
