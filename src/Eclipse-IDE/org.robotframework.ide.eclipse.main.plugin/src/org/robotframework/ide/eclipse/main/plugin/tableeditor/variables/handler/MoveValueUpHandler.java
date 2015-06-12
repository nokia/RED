package org.robotframework.ide.eclipse.main.plugin.tableeditor.variables.handler;

import javax.inject.Named;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.tools.compat.parts.DIHandler;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.ISources;
import org.eclipse.ui.PlatformUI;
import org.robotframework.ide.eclipse.main.plugin.RobotCollectionElement;
import org.robotframework.ide.eclipse.main.plugin.cmd.MoveValueUpCommand;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.variables.handler.MoveValueUpHandler.E4MoveValueUpHandler;
import org.robotframework.viewers.Selections;

public class MoveValueUpHandler extends DIHandler<E4MoveValueUpHandler> {

    public MoveValueUpHandler() {
        super(E4MoveValueUpHandler.class);
    }

    public static class E4MoveValueUpHandler {

        //@Inject
        //private RobotEditorCommandsStack stack;

        @Execute
        public Object moveValueUp(@Named(ISources.ACTIVE_MENU_SELECTION_NAME) final IStructuredSelection selection) {

            
            final RobotCollectionElement selectedValue = Selections.getSingleElement(selection, RobotCollectionElement.class);
            
            final IEclipseContext context = ((IEclipseContext) PlatformUI.getWorkbench().getService(IEclipseContext.class))
                    .getActiveLeaf();
            MoveValueUpCommand command = new MoveValueUpCommand(selectedValue);
            ContextInjectionFactory.inject(command, context);
            command.execute();
            //stack.execute(new MoveVariableUpCommand(variablesSection, index));
            return null;
        }
    }
}
