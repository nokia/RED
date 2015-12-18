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
        if ("isInternalFolder".equals(property)) {
            return projectElement.isInternalFolder() == expected;
        } else if ("isIncluded".equals(property)) {
            return !projectElement.isExcluded() == expected;
        } else if ("isExcluded".equals(property)) {
            return projectElement.isExcluded() == expected;
        } else if ("isNonVirtual".equals(property)) {
            return !projectElement.isVirtual() == expected;
        }
        return false;
    }

}
