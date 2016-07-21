/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.cases.handler;

import javax.inject.Inject;
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

        @Inject
        private RobotEditorCommandsStack stack;

        @Execute
        public void addNewTestCase(@Named(RobotEditorSources.SUITE_FILE_MODEL) final RobotSuiteFile fileModel,
                @Named(Selections.SELECTION) final IStructuredSelection selection) {
            Selections.assureSingleElementIsSelected(selection);

            RobotCase testCase = null;

            final Optional<RobotElement> selectedElement = Selections.getOptionalFirstElement(selection,
                    RobotElement.class);
            if (selectedElement.isPresent() && selectedElement.get() instanceof RobotKeywordCall) {
                testCase = (RobotCase) selectedElement.get().getParent();
            } else if (selectedElement.isPresent() && selectedElement.get() instanceof RobotCase) {
                testCase = (RobotCase) selectedElement.get();
            } else {
                final AddingToken token = Selections.getSingleElement(selection, AddingToken.class);
                testCase = (RobotCase) token.getParent();
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
