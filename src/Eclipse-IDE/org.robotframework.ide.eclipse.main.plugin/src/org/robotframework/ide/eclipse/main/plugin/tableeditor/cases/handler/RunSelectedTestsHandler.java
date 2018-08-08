/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.cases.handler;

import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCasesSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCodeHoldingElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotFileInternalElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotTasksSection;
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

            final List<RobotFileInternalElement> elements = Selections.getElements(selection,
                    RobotFileInternalElement.class);
            if (elements.isEmpty()) {
                return;
            }
            final List<RobotCodeHoldingElement<?>> testCasesToCall = getTestCasesToCall(elements);
            if (!testCasesToCall.isEmpty()) {
                RunSelectedTestCasesAction.runSelectedTestCases(new StructuredSelection(testCasesToCall),
                        Mode.valueOf(mode));
                return;
            }
            final List<RobotFileInternalElement> testSuites = elements.stream()
                    .filter(elem -> elem instanceof RobotCasesSection || elem instanceof RobotTasksSection)
                    .collect(toList());
            RunSelectedTestCasesAction.runSelectedTestCases(new StructuredSelection(testSuites), Mode.valueOf(mode));
        }

        private static List<RobotCodeHoldingElement<?>> getTestCasesToCall(
                final List<RobotFileInternalElement> elements) {
            final List<RobotCodeHoldingElement<?>> testCasesToCall = new ArrayList<>();
            for (final RobotFileInternalElement elem : elements) {
                if (elem instanceof RobotKeywordCall) {
                    testCasesToCall.add((RobotCodeHoldingElement<?>) ((RobotKeywordCall) elem).getParent());

                } else if (elem instanceof RobotCodeHoldingElement<?>) {
                    testCasesToCall.add((RobotCodeHoldingElement<?>) elem);
                }
            }
            return testCasesToCall;
        }
    }

}
