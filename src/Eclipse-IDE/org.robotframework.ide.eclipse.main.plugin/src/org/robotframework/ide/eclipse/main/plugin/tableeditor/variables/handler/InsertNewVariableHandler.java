/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.variables.handler;

import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.rf.ide.core.testdata.model.table.variables.AVariable.VariableType;
import org.robotframework.ide.eclipse.main.plugin.model.RobotVariable;
import org.robotframework.ide.eclipse.main.plugin.model.RobotVariablesSection;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.variables.CreateFreshVariableCommand;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorCommandsStack;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.variables.handler.InsertNewVariableHandler.E4InsertNewHandler;
import org.robotframework.red.commands.DIParameterizedHandler;
import org.robotframework.red.viewers.Selections;

public class InsertNewVariableHandler extends DIParameterizedHandler<E4InsertNewHandler> {

    public InsertNewVariableHandler() {
        super(E4InsertNewHandler.class);
    }

    public static class E4InsertNewHandler {

        @Execute
        public void addNewVariable(@Named(Selections.SELECTION) final IStructuredSelection selection,
                final RobotEditorCommandsStack stack,
                @Named("org.robotframework.ide.eclipse.insertNew.variableType") final String place) {
            final RobotVariable selectedVariable = Selections.getSingleElement(selection, RobotVariable.class);
            final RobotVariablesSection variablesSection = selectedVariable.getParent();
            final int index = variablesSection.getChildren().indexOf(selectedVariable);
            
            stack.execute(new CreateFreshVariableCommand(variablesSection, index, VariableType.valueOf(place)));
        }
    }
}
