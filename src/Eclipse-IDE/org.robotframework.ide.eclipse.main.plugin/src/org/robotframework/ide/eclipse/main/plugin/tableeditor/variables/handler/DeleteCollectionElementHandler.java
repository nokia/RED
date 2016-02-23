/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.variables.handler;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.variables.RobotCollectionElement;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.variables.handler.DeleteCollectionElementHandler.E4DeleteCollectionElementHandler;
import org.robotframework.red.commands.DIParameterizedHandler;
import org.robotframework.red.viewers.Selections;

public class DeleteCollectionElementHandler extends DIParameterizedHandler<E4DeleteCollectionElementHandler> {

    public DeleteCollectionElementHandler() {
        super(E4DeleteCollectionElementHandler.class);
    }

    public static class E4DeleteCollectionElementHandler {

        @Inject
        protected IEventBroker eventBroker;

        @Execute
        public Object deleteCollectionElement(@Named(Selections.SELECTION) final IStructuredSelection selection) {

            final List<RobotCollectionElement> variables = Selections.getElements(selection, RobotCollectionElement.class);
            if (!variables.isEmpty()) {
                eventBroker.send(RobotModelEvents.ROBOT_VARIABLE_COLLECTION_ELEMENT_DELETE, variables.get(0));
            }
            
            return null;
        }
    }
}
