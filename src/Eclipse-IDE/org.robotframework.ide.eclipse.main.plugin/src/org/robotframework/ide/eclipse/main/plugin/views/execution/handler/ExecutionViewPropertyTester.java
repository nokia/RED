/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.views.execution.handler;

import org.eclipse.core.expressions.PropertyTester;
import org.robotframework.ide.eclipse.main.plugin.launch.RobotTestExecutionService.RobotTestsLaunch;
import org.robotframework.ide.eclipse.main.plugin.views.execution.ExecutionStatusStore;
import org.robotframework.ide.eclipse.main.plugin.views.execution.ExecutionView;
import org.robotframework.ide.eclipse.main.plugin.views.execution.ExecutionViewWrapper;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;

public class ExecutionViewPropertyTester extends PropertyTester {

    public static final String NAMESPACE = "org.robotframework.red.view.execution";

    @VisibleForTesting static final String CURRENT_LAUNCH_IS_TERMINATED = "currentLaunchIsTerminated";
    public static final String PROPERTY_CURRENT_LAUNCH_IS_TERMINATED = NAMESPACE + "." + CURRENT_LAUNCH_IS_TERMINATED;
    
    @VisibleForTesting static final String CURRENT_LAUNCH_EXEC_STORE_IS_DISPOSED = "currentLaunchExecStoreIsDisposed";
    public static final String PROPERTY_CURRENT_LAUNCH_EXEC_STORE_IS_DISPOSED = NAMESPACE + "." + CURRENT_LAUNCH_EXEC_STORE_IS_DISPOSED;
    
    @VisibleForTesting static final String CURRENT_LAUNCH_HAS_FAILED_TEST = "currentLaunchHasFailedTest";
    public static final String PROPERTY_CURRENT_LAUNCH_HAS_FAILED_TEST = NAMESPACE + "." + CURRENT_LAUNCH_HAS_FAILED_TEST;

    @Override
    public boolean test(final Object receiver, final String property, final Object[] args, final Object expectedValue) {
        Preconditions.checkArgument(receiver instanceof ExecutionViewWrapper,
                "Property tester is unable to test properties of " + receiver.getClass().getName()
                        + ". It should be used with " + ExecutionViewWrapper.class.getName());

        @SuppressWarnings("restriction")
        final ExecutionView view = ((ExecutionViewWrapper) receiver).getComponent();
        if (expectedValue instanceof Boolean) {
            return testProperty(view, property, ((Boolean) expectedValue).booleanValue());
        }
        return false;
    }

    private boolean testProperty(final ExecutionView view, final String property,
            final boolean expectedValue) {
        if (CURRENT_LAUNCH_IS_TERMINATED.equals(property)) {
            final boolean isTerminated = view.getCurrentlyShownLaunch()
                    .map(RobotTestsLaunch::isTerminated)
                    .orElse(false);
            return isTerminated == expectedValue;
        } else if (CURRENT_LAUNCH_EXEC_STORE_IS_DISPOSED.equals(property)) {
            final boolean isDisposed = view.getCurrentlyShownLaunch()
                    .flatMap(launch -> launch.getExecutionData(ExecutionStatusStore.class))
                    .map(ExecutionStatusStore::isDisposed)
                    .orElse(false);
            return isDisposed == expectedValue;
        } else if (CURRENT_LAUNCH_HAS_FAILED_TEST.equals(property)) {
            final int numberOfFailedTests = view.getCurrentlyShownLaunch()
                    .flatMap(launch -> launch.getExecutionData(ExecutionStatusStore.class))
                    .map(ExecutionStatusStore::getFailedTests)
                    .orElse(0);
            return numberOfFailedTests > 0 == expectedValue;
        }
        return false;
    }
}
