/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.variables.handler;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.model.RobotVariable;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.variables.handler.EditVariableHandler.E4EditVariableHandler;
import org.robotframework.red.commands.DIParameterizedHandler;
import org.robotframework.red.viewers.Selections;

public class EditVariableHandler extends DIParameterizedHandler<E4EditVariableHandler> {

    public EditVariableHandler() {
        super(E4EditVariableHandler.class);
    }

    public static class E4EditVariableHandler {

        @Inject
        protected IEventBroker eventBroker;

        @Execute
        public Object editVariable(@Named(Selections.SELECTION) final IStructuredSelection selection) {
            final RobotVariable selectedVariable = Selections.getSingleElement(selection, RobotVariable.class);
            eventBroker.send(RobotModelEvents.ROBOT_VARIABLE_EDIT, selectedVariable);
            
            return null;
        }
    }
}
