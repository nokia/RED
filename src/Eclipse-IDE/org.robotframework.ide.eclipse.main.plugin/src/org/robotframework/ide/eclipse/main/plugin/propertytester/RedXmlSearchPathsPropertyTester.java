/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.propertytester;

import org.eclipse.core.expressions.PropertyTester;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfig.SearchPath;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;


public class RedXmlSearchPathsPropertyTester extends PropertyTester {

    @VisibleForTesting static final String IS_SYSTEM_PATH = "isSystemPath";

    @Override
    public boolean test(final Object receiver, final String property, final Object[] args, final Object expectedValue) {
        Preconditions.checkArgument(receiver instanceof SearchPath,
                "Property tester is unable to test properties of " + receiver.getClass().getName()
                        + ". It should be used with " + SearchPath.class.getName());

        if (expectedValue instanceof Boolean) {
            final boolean result = testProperty((SearchPath) receiver, property,
                    ((Boolean) expectedValue).booleanValue());
            return result;
        }
        return false;
    }

    private boolean testProperty(final SearchPath searchPath, final String property, final boolean expected) {
        if (IS_SYSTEM_PATH.equals(property)) {
            return searchPath.isSystem() == expected;
        }
        return false;
    }
}
