/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.keywords.handler;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordDefinition;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordsSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFileSection;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.CreateFreshKeywordDefinitionCommand;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorCommandsStack;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.keywords.handler.InsertNewKeywordHandler.E4InsertNewKeywordHandler;
import org.robotframework.red.commands.DIParameterizedHandler;
import org.robotframework.red.viewers.Selections;

public class InsertNewKeywordHandler extends DIParameterizedHandler<E4InsertNewKeywordHandler> {

    public InsertNewKeywordHandler() {
        super(E4InsertNewKeywordHandler.class);
    }

    public static class E4InsertNewKeywordHandler {
        @Inject
        private RobotEditorCommandsStack stack;

        @Execute
        public Object addNewUserDefinedKeyword(@Named(Selections.SELECTION) final IStructuredSelection selection) {
            final RobotElement selectedElement = Selections.getSingleElement(selection, RobotElement.class);

            RobotSuiteFileSection section = null;
            RobotKeywordDefinition definition = null;
            if (selectedElement instanceof RobotKeywordCall) {
                definition = (RobotKeywordDefinition) selectedElement.getParent();
                section = ((RobotKeywordCall) selectedElement).getSection();
            } else if (selectedElement instanceof RobotKeywordDefinition) {
                definition = (RobotKeywordDefinition) selectedElement;
                section = definition.getParent();
            }

            if (section == null || definition == null) {
                return null;
            }

            final int index = section.getChildren().indexOf(definition);
            stack.execute(new CreateFreshKeywordDefinitionCommand((RobotKeywordsSection) section, index));
            return null;
        }
    }
}
