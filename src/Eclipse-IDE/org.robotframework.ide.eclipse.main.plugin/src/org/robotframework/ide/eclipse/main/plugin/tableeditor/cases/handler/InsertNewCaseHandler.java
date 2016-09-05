/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.cases.handler;

import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCase;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCasesSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFileSection;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.cases.CreateFreshCaseCommand;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.AddingToken;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorCommandsStack;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorSources;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.cases.handler.InsertNewCaseHandler.E4InsertNewCaseHandler;
import org.robotframework.red.commands.DIParameterizedHandler;
import org.robotframework.red.viewers.Selections;

import com.google.common.base.Optional;

public class InsertNewCaseHandler extends DIParameterizedHandler<E4InsertNewCaseHandler> {

    public InsertNewCaseHandler() {
        super(E4InsertNewCaseHandler.class);
    }

    public static class E4InsertNewCaseHandler {

        @Execute
        public void addNewTestCase(@Named(RobotEditorSources.SUITE_FILE_MODEL) final RobotSuiteFile fileModel,
                @Named(Selections.SELECTION) final IStructuredSelection selection, final RobotEditorCommandsStack stack) {

            if (selection.size() > 1) {
                throw new IllegalArgumentException("Given selection should contain at most one element, but have "
                        + selection.size() + " instead");
            }

            RobotCase testCase = null;

            final Optional<RobotElement> selectedElement = Selections.getOptionalFirstElement(selection,
                    RobotElement.class);
            if (selectedElement.isPresent() && selectedElement.get() instanceof RobotKeywordCall) {
                testCase = (RobotCase) selectedElement.get().getParent();
            } else if (selectedElement.isPresent() && selectedElement.get() instanceof RobotCase) {
                testCase = (RobotCase) selectedElement.get();
            }
            final Optional<AddingToken> token = Selections.getOptionalFirstElement(selection, AddingToken.class);
            if (token.isPresent()) {
                testCase = (RobotCase) token.get().getParent();
            }

            if (testCase == null) {
                final RobotCasesSection section = fileModel.findSection(RobotCasesSection.class).get();
                stack.execute(new CreateFreshCaseCommand(section));

            } else if (testCase != null) {
                final RobotSuiteFileSection section = testCase.getParent();
                final int index = section.getChildren().indexOf(testCase);
                stack.execute(new CreateFreshCaseCommand((RobotCasesSection) section, index));
            }
        }
    }
}
