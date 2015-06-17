package org.robotframework.ide.eclipse.main.plugin.tableeditor.cases.handler;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.tools.compat.parts.DIHandler;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.robotframework.ide.eclipse.main.plugin.RobotCase;
import org.robotframework.ide.eclipse.main.plugin.RobotCasesSection;
import org.robotframework.ide.eclipse.main.plugin.cmd.CreateFreshCaseCommand;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorCommandsStack;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.cases.handler.InsertNewCaseHandler.E4InsertNewCaseHandler;
import org.robotframework.viewers.Selections;

public class InsertNewCaseHandler extends DIHandler<E4InsertNewCaseHandler> {

    public InsertNewCaseHandler() {
        super(E4InsertNewCaseHandler.class);
    }

    public static class E4InsertNewCaseHandler {

        @Inject
        private RobotEditorCommandsStack stack;

        @Execute
        public Object addNewTestCase(@Named(Selections.SELECTION) final IStructuredSelection selection) {
            final RobotCase selectedCase = Selections.getSingleElement(selection, RobotCase.class);
            final RobotCasesSection casesSection = (RobotCasesSection) selectedCase.getParent();
            final int index = casesSection.getChildren().indexOf(selectedCase);

            stack.execute(new CreateFreshCaseCommand(casesSection, index));
            return null;
        }
    }
}
