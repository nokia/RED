package org.robotframework.ide.eclipse.main.plugin.tableeditor.variables.handler;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.tools.compat.parts.DIHandler;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.robotframework.ide.eclipse.main.plugin.RobotCollectionElement;
import org.robotframework.ide.eclipse.main.plugin.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.variables.handler.DeleteCollectionElementHandler.E4DeleteCollectionElementHandler;
import org.robotframework.red.viewers.Selections;

public class DeleteCollectionElementHandler extends DIHandler<E4DeleteCollectionElementHandler> {

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
