/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.cases.handler;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;

import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCase;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCasesSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.navigator.actions.RunSelectedTestCasesAction;
import org.robotframework.ide.eclipse.main.plugin.navigator.actions.RunSelectedTestCasesAction.Mode;
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
                final List<RobotCase> testCasesToCall = getTestCasesToCall(selection);
                if (!testCasesToCall.isEmpty()) {
                    RunSelectedTestCasesAction.runSelectedTestCases(new StructuredSelection(testCasesToCall),
                            Mode.valueOf(mode));
                } else {
                    final List<RobotCasesSection> testSuites = getTestCasesSectionsToCall(selection);
                    RunSelectedTestCasesAction.runSelectedTestCases(new StructuredSelection(testSuites),
                            Mode.valueOf(mode));
                }
            }
        }

        private static List<RobotCase> getTestCasesToCall(final IStructuredSelection selection) {
            final List<RobotCase> testCasesToCall = newArrayList();
            for (final Object o : selection.toList()) {
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
            return testCasesToCall;
        }

        private static List<RobotCasesSection> getTestCasesSectionsToCall(final IStructuredSelection selection) {
            final List<RobotCasesSection> testCasesSectionsToCall = newArrayList();
            for (final Object o : selection.toList()) {
                RobotCasesSection section = null;
                if (o instanceof RobotCasesSection) {
                    section = (RobotCasesSection) o;
                    testCasesSectionsToCall.add(section);
                }
            }
            return testCasesSectionsToCall;
        }
    }

}
