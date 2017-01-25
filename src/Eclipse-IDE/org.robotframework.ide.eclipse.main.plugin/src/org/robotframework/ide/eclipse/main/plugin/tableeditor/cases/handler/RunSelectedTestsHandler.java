/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.cases.handler;

import java.util.ArrayList;

import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCase;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.navigator.actions.RunTestCaseAction;
import org.robotframework.ide.eclipse.main.plugin.navigator.actions.RunTestCaseAction.Mode;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.cases.handler.RunSelectedTestsHandler.E4RunTestHandler;
import org.robotframework.red.commands.DIParameterizedHandler;
import org.robotframework.red.viewers.Selections;

public class RunSelectedTestsHandler extends DIParameterizedHandler<E4RunTestHandler> {

    public RunSelectedTestsHandler() {
        super(E4RunTestHandler.class);
    }

    public static class E4RunTestHandler {

        @Execute
        public void runSelectedTests(final @Named(Selections.SELECTION) IStructuredSelection selection,
                @Named(RunTestFromTableDynamicMenuItem.RUN_TEST_COMMAND_MODE_PARAMETER) final String mode) {

            if (!selection.isEmpty()) {
                ArrayList<RobotCase> testCasesToCall = new ArrayList<RobotCase>();
                for (Object o : ((StructuredSelection) selection).toList()) {
                    RobotCase testCase = null;
                    if (o instanceof RobotKeywordCall) {
                        testCase = (RobotCase) ((RobotKeywordCall) o).getParent();
                    } else if (o instanceof RobotCase) {
                        testCase = (RobotCase) o;
                    }
                    if (testCase != null) {
                        testCasesToCall.add(testCase);
                    }
                }
                RunTestCaseAction.runSelectedTestCases(new StructuredSelection(testCasesToCall), Mode.valueOf(mode));
            }

        }
    }

}
