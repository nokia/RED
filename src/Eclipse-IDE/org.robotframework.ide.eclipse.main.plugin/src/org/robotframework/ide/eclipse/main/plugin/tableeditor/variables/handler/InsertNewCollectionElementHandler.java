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
import org.robotframework.ide.eclipse.main.plugin.tableeditor.variables.RobotCollectionElement;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.variables.handler.InsertNewCollectionElementHandler.E4InsertNewCollectionElementHandler;
import org.robotframework.red.commands.DIParameterizedHandler;
import org.robotframework.red.viewers.Selections;

public class InsertNewCollectionElementHandler extends DIParameterizedHandler<E4InsertNewCollectionElementHandler> {

    public InsertNewCollectionElementHandler() {
        super(E4InsertNewCollectionElementHandler.class);
    }

    public static class E4InsertNewCollectionElementHandler {

        @Inject
        protected IEventBroker eventBroker;

        @Execute
        public Object insertCollectionElement(@Named(Selections.SELECTION) final IStructuredSelection selection) {

            final RobotCollectionElement selectedValue = Selections.getSingleElement(selection,
                    RobotCollectionElement.class);
            eventBroker.send(RobotModelEvents.ROBOT_VARIABLE_COLLECTION_ELEMENT_INSERT, selectedValue);

            return null;
        }
    }
}
