/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.propertytester;

import java.util.List;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteStreamFile;
import org.robotframework.red.viewers.Selections;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;

public class SelectionsPropertyTester extends PropertyTester {

    @VisibleForTesting static final String ALL_ELEMENTS_HAVE_SAME_TYPE = "allElementsHaveSameType";

    @VisibleForTesting
    static final String SELECTED_ACTUAL_FILE = "selectedActualFile";

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
            return testIfAllElementsHaveSameType(selection, expected);
        } else if (SELECTED_ACTUAL_FILE.equals(property)) {
            return testIfIsSelectedActualFile(selection, expected);
        }
        return false;
    }

    private boolean testIfIsSelectedActualFile(final IStructuredSelection selection, final boolean expected) {
        Object firstSelectedElement = null;
        if (selection.isEmpty()) {
            return expected;
        } else {
            firstSelectedElement = selection.getFirstElement();
        }
        while (firstSelectedElement instanceof RobotElement) {
            RobotElement robotElement = (RobotElement) firstSelectedElement;
            if (robotElement.getParent() != null) {
                firstSelectedElement = robotElement.getParent();
            } else {
                break;
            }
        }
        if (firstSelectedElement instanceof RobotSuiteStreamFile) {
            return false;
        }
        return true;
    }

    private boolean testIfAllElementsHaveSameType(final IStructuredSelection selection, final boolean expected) {
        final List<Object> elements = Selections.getElements(selection, Object.class);
        if (elements.isEmpty()) {
            return expected;
        }
        Class<?> mostGeneralType = elements.get(0).getClass();
        for (final Object element : elements) {
            if (element.getClass().isAssignableFrom(mostGeneralType)) {
                mostGeneralType = element.getClass();
            }
        }
        if (mostGeneralType == Object.class) {
            return !expected;
        }
        for (final Object element : elements) {
            if (!mostGeneralType.isInstance(element)) {
                return !expected;
            }
        }
        return expected;
    }

}
