package org.robotframework.ide.eclipse.main.plugin.tableeditor.cases.handler;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.tools.compat.parts.DIHandler;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.robotframework.ide.eclipse.main.plugin.RobotCase;
import org.robotframework.ide.eclipse.main.plugin.RobotCasesSection;
import org.robotframework.ide.eclipse.main.plugin.cmd.MoveCaseDownCommand;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorCommandsStack;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.cases.handler.MoveCaseDownHandler.E4MoveCaseDownHandler;
import org.robotframework.viewers.Selections;

public class MoveCaseDownHandler extends DIHandler<E4MoveCaseDownHandler> {

    public MoveCaseDownHandler() {
        super(E4MoveCaseDownHandler.class);
    }

    public static class E4MoveCaseDownHandler {

        @Inject
        private RobotEditorCommandsStack stack;

        @Execute
        public Object moveCaseDown(@Named(Selections.SELECTION) final IStructuredSelection selection) {
            final RobotCase selectedCase = Selections.getSingleElement(selection, RobotCase.class);
            final RobotCasesSection casesSection = (RobotCasesSection) selectedCase.getParent();
            final int index = casesSection.getChildren().indexOf(selectedCase);

            stack.execute(new MoveCaseDownCommand(casesSection, index));
            return null;
        }
    }
}
