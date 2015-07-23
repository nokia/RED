package org.robotframework.ide.eclipse.main.plugin.tableeditor.variables.handler;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.tools.compat.parts.DIHandler;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.robotframework.ide.eclipse.main.plugin.RobotCollectionElement;
import org.robotframework.ide.eclipse.main.plugin.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.variables.handler.MoveCollectionElementDownHandler.E4MoveCollectionElementDownHandler;
import org.robotframework.red.viewers.Selections;

public class MoveCollectionElementDownHandler extends DIHandler<E4MoveCollectionElementDownHandler> {

    public MoveCollectionElementDownHandler() {
        super(E4MoveCollectionElementDownHandler.class);
    }

    public static class E4MoveCollectionElementDownHandler {

        @Inject
        protected IEventBroker eventBroker;

        @Execute
        public Object moveCollectionElementDown(@Named(Selections.SELECTION) final IStructuredSelection selection) {

            final RobotCollectionElement selectedValue = Selections.getSingleElement(selection,
                    RobotCollectionElement.class);
            eventBroker.send(RobotModelEvents.ROBOT_VARIABLE_COLLECTION_ELEMENT_MOVE_DOWN, selectedValue);

            return null;
        }
    }
}
