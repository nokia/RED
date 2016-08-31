/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.cases.handler;

import java.util.Arrays;

import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ISources;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCase;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.navigator.actions.RunTestCaseAction;
import org.robotframework.ide.eclipse.main.plugin.navigator.actions.RunTestCaseAction.Mode;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorSources;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotFormEditor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.cases.handler.RunTestHandler.E4RunTestHandler;
import org.robotframework.red.commands.DIParameterizedHandler;

public class RunTestHandler extends DIParameterizedHandler<E4RunTestHandler> {

    public RunTestHandler() {
        super(E4RunTestHandler.class);
    }

    public static class E4RunTestHandler {

        @Execute
        public void runSingleTest(final @Named(ISources.ACTIVE_EDITOR_NAME) RobotFormEditor editor,
                @Named(RobotEditorSources.SUITE_FILE_MODEL) final RobotSuiteFile suiteModel,
                @Named(RunTestFromTableDynamicMenuItem.RUN_TEST_COMMAND_MODE_PARAMETER) final String mode) {

            final IEditorPart activeEditor = editor.getActiveEditor();
            final ISelectionProvider selectionProvider = activeEditor.getEditorSite().getSelectionProvider();
            if (selectionProvider != null) {
                final ISelection selection = selectionProvider.getSelection();
                if (selection instanceof StructuredSelection && !selection.isEmpty()) {
                    final Object firstElement = ((StructuredSelection) selection).getFirstElement();
                    RobotCase testCase = null;
                    if (firstElement instanceof RobotKeywordCall) {
                        testCase = (RobotCase) ((RobotKeywordCall) firstElement).getParent();
                    } else if (firstElement instanceof RobotCase) {
                        testCase = (RobotCase) firstElement;
                    }
                    
                    if (testCase != null) {
                        RunTestCaseAction.runTestCase(new StructuredSelection(Arrays.asList(testCase)),
                                Mode.valueOf(mode));
                    }
                }
            }

        }
    }

}
