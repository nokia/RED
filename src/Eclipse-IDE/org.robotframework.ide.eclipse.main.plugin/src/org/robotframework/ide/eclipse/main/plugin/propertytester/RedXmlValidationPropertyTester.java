/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.propertytester;

import org.eclipse.core.expressions.PropertyTester;
import org.robotframework.ide.eclipse.main.plugin.project.editor.validation.ProjectTreeElement;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;

public class RedXmlValidationPropertyTester extends PropertyTester {

    @VisibleForTesting
    static final String IS_EXCLUDED = "isExcluded";

    @VisibleForTesting
    static final String IS_INTERNAL_FOLDER = "isInternalFolder";

    @VisibleForTesting
    static final String IS_FILE = "isFile";

    @VisibleForTesting
    static final String PARENT_EXCLUDED = "isExcludedViaInheritance";

    @VisibleForTesting
    static final String IS_PROJECT = "isProject";

    @Override
    public boolean test(final Object receiver, final String property, final Object[] args, final Object expectedValue) {
        Preconditions.checkArgument(receiver instanceof ProjectTreeElement,
                "Property tester is unable to test properties of " + receiver.getClass().getName()
                        + ". It should be used with " + ProjectTreeElement.class.getName());

        if (expectedValue instanceof Boolean) {
            return testProperty((ProjectTreeElement) receiver, property, ((Boolean) expectedValue).booleanValue());
        }
        return false;
    }

    private boolean testProperty(final ProjectTreeElement projectElement, final String property,
            final boolean expected) {
        if (IS_INTERNAL_FOLDER.equals(property)) {
            return projectElement.isInternalFolder() == expected;
        } else if (IS_EXCLUDED.equals(property)) {
            return projectElement.isExcluded() == expected;
        } else if (IS_FILE.equals(property)) {
            return projectElement.isFile() == expected;
        } else if (PARENT_EXCLUDED.equals(property)) {
            return projectElement.isExcludedViaInheritance() == expected;
        } else if (IS_PROJECT.equals(property)) {
            return projectElement.isProject() == expected;
        }
        return false;
    }

}
