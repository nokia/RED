/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.views.execution.handler;

import org.eclipse.core.expressions.PropertyTester;
import org.robotframework.ide.eclipse.main.plugin.views.execution.ExecutionTreeNode;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;

public class ExecutionViewNodesPropertyTester extends PropertyTester {

    @VisibleForTesting static final String KIND = "kind";

    @Override
    public boolean test(final Object receiver, final String property, final Object[] args, final Object expectedValue) {
        Preconditions.checkArgument(receiver instanceof ExecutionTreeNode,
                "Property tester is unable to test properties of " + receiver.getClass().getName()
                        + ". It should be used with " + ExecutionTreeNode.class.getName());

        if (expectedValue instanceof String) {
            return testProperty((ExecutionTreeNode) receiver, property, (String) expectedValue);
        }
        return false;
    }

    private boolean testProperty(final ExecutionTreeNode executionNode, final String property,
            final String expectedValue) {
        if (KIND.equals(property)) {
            return executionNode.getKind().name().equalsIgnoreCase(expectedValue);
        }
        return false;
    }
}
