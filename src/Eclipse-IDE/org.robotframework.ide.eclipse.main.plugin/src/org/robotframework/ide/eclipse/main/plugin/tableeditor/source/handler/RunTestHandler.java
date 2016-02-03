/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.source.handler;

import java.util.Arrays;

import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.ISources;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCase;
import org.robotframework.ide.eclipse.main.plugin.model.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.navigator.actions.RunTestCaseAction;
import org.robotframework.ide.eclipse.main.plugin.navigator.actions.RunTestCaseAction.Mode;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorSources;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotFormEditor;
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
        public Object runSingleTest(final @Named(ISources.ACTIVE_EDITOR_NAME) RobotFormEditor editor,
                @Named(RobotEditorSources.SUITE_FILE_MODEL) final RobotSuiteFile suiteModel,
                @Named(RunTestDynamicMenuItem.RUN_TEST_COMMAND_MODE_PARAMETER) final String mode) {

            final SourceViewer viewer = editor.getSourceEditor().getViewer();
            final ITextSelection selection = (ITextSelection) viewer.getSelection();
            final RobotCase testCase = getTestCase(suiteModel, selection.getOffset());

            if (testCase != null) {
                RunTestCaseAction.runTestCase(new StructuredSelection(Arrays.asList(testCase)), Mode.valueOf(mode));
            }
            return null;
        }
    }

    static RobotCase getTestCase(final RobotSuiteFile suiteModel, final int caretOffset) {
        final Optional<? extends RobotElement> element = suiteModel.findElement(caretOffset);
        if (element.isPresent()) {
            final RobotElement elem = element.get();
            if (elem instanceof RobotCase) {
                return (RobotCase) elem;
            } else if (elem instanceof RobotKeywordCall && elem.getParent() instanceof RobotCase) {
                return (RobotCase) elem.getParent();
            }
        }
        return null;
    }
}
