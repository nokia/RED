/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.cases.handler;

import java.util.List;

import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCasesSection;
import org.robotframework.ide.eclipse.main.plugin.navigator.actions.RunTestSuiteAction;
import org.robotframework.ide.eclipse.main.plugin.navigator.actions.RunTestSuiteAction.Mode;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.cases.handler.RunTestsFromSelectionHandler.E4RunTestHandler;
import org.robotframework.red.commands.DIParameterizedHandler;
import org.robotframework.red.viewers.Selections;

public class RunTestsFromSelectionHandler extends DIParameterizedHandler<E4RunTestHandler> {

    public RunTestsFromSelectionHandler() {
        super(E4RunTestHandler.class);
    }

    public static class E4RunTestHandler {

        @Execute
        public void runSelectedTests(final @Named(Selections.SELECTION) IStructuredSelection selection,
                @Named(RunTestFromTableDynamicMenuItem.RUN_TEST_COMMAND_MODE_PARAMETER) final String mode) {

            if (!selection.isEmpty()) {
                final List<RobotCasesSection> testSuites = Selections.getElements(selection, RobotCasesSection.class);
                RunTestSuiteAction.runTestSuite(new StructuredSelection(testSuites), Mode.valueOf(mode));
            }

        }
    }

}
