/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.source.handler;

import java.util.Arrays;

import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.jface.viewers.StructuredSelection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCase;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCasesSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.navigator.actions.RunTestCaseAction;
import org.robotframework.ide.eclipse.main.plugin.navigator.actions.RunTestCaseAction.Mode;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorSources;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.handler.RunTestHandler.E4RunTestHandler;
import org.robotframework.red.commands.DIParameterizedHandler;

import com.google.common.base.Optional;

/**
 * @author Michal Anglart
 *
 */
public class RunTestHandler extends DIParameterizedHandler<E4RunTestHandler> {

    public RunTestHandler() {
        super(E4RunTestHandler.class);
    }

    public static class E4RunTestHandler {

        @Execute
        public Object runSingleTest(@Named(RobotEditorSources.SUITE_FILE_MODEL) final RobotSuiteFile suiteModel,
                @Named(RunTestDynamicMenuItem.RUN_TEST_COMMAND_MODE_PARAMETER) final String mode,
                @Named(RunTestDynamicMenuItem.RUN_TEST_COMMAND_TEST_NAME_PARAMETER) final String testName) {

            final Optional<RobotCasesSection> casesSection = suiteModel.findSection(RobotCasesSection.class);
            if (casesSection.isPresent()) {
                for (final RobotCase testCase : casesSection.get().getChildren()) {
                    if (testCase.getName().equals(testName)) {
                        final StructuredSelection selection = new StructuredSelection(Arrays.asList(testCase));
                        RunTestCaseAction.runTestCase(selection, Mode.valueOf(mode));
                        break;
                    }
                }
            }
            return null;
        }
    }
}
