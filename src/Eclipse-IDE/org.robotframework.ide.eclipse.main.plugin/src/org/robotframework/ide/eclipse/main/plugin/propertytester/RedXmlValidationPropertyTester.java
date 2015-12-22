/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.propertytester;

import org.eclipse.core.expressions.PropertyTester;
import org.robotframework.ide.eclipse.main.plugin.project.editor.validation.ProjectTreeElement;

import com.google.common.base.Preconditions;


public class RedXmlValidationPropertyTester extends PropertyTester {

    public static final String NAMESPACE = "org.robotframework.redxml.validation";

    public static final String IS_EXCLUDED = "isExcluded";
    public static final String PROPERTY_IS_EXCLUDED = NAMESPACE + "." + IS_EXCLUDED;

    public static final String IS_INCLUDED = "isIncluded";
    public static final String PROPERTY_IS_INCLUDED = NAMESPACE + "." + IS_INCLUDED;

    public static final String IS_INTERNAL_FOLDER = "isInternalFolder";
    public static final String PROPERTY_IS_INTERNAL_FOLDER = NAMESPACE + "." + IS_INTERNAL_FOLDER;

    @Override
    public boolean test(final Object receiver, final String property, final Object[] args, final Object expectedValue) {
        Preconditions.checkArgument(receiver instanceof ProjectTreeElement,
                "Property tester is unable to test properties of " + receiver.getClass().getName()
                        + ". It should be used with " + ProjectTreeElement.class.getName());

        if (expectedValue instanceof Boolean) {
            final boolean result = testProperty((ProjectTreeElement) receiver, property, ((Boolean) expectedValue).booleanValue());
            return result;
        }
        return false;
    }

    private boolean testProperty(final ProjectTreeElement projectElement, final String property,
            final boolean expected) {
        if (IS_INTERNAL_FOLDER.equals(property)) {
            return projectElement.isInternalFolder() == expected;
        } else if (IS_INCLUDED.equals(property)) {
            final boolean isIncluded = !projectElement.isExcluded();
            return isIncluded == expected;
        } else if (IS_EXCLUDED.equals(property)) {
            return projectElement.isExcluded() == expected;
        }
        return false;
    }

}
