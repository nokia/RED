package org.robotframework.ide.eclipse.main.plugin.tableeditor.cases.handler;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.tools.compat.parts.DIHandler;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.robotframework.ide.eclipse.main.plugin.RobotCase;
import org.robotframework.ide.eclipse.main.plugin.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.cmd.MoveCaseUpCommand;
import org.robotframework.ide.eclipse.main.plugin.cmd.MoveKeywordCallUpCommand;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorCommandsStack;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.cases.handler.MoveCaseUpHandler.E4MoveCaseUpHandler;
import org.robotframework.red.viewers.Selections;

import com.google.common.base.Optional;

public class MoveCaseUpHandler extends DIHandler<E4MoveCaseUpHandler> {

    public MoveCaseUpHandler() {
        super(E4MoveCaseUpHandler.class);
    }

    public static class E4MoveCaseUpHandler {

        @Inject
        private RobotEditorCommandsStack commandsStack;

        @Execute
        public Object moveCaseUp(@Named(Selections.SELECTION) final IStructuredSelection selection) {
            final Optional<RobotKeywordCall> maybeKeywordCall = Selections.getOptionalFirstElement(selection,
                    RobotKeywordCall.class);
            final Optional<RobotCase> maybeTestCase = Selections.getOptionalFirstElement(selection, RobotCase.class);

            if (maybeKeywordCall.isPresent()) {
                commandsStack.execute(new MoveKeywordCallUpCommand(maybeKeywordCall.get()));
            } else if (maybeTestCase.isPresent()) {
                commandsStack.execute(new MoveCaseUpCommand(maybeTestCase.get()));
            }
            return null;
        }
    }
}
