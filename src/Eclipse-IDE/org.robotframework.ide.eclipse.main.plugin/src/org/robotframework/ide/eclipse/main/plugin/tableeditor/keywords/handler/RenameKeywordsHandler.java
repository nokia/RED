package org.robotframework.ide.eclipse.main.plugin.tableeditor.keywords.handler;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.tools.compat.parts.DIHandler;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorCommandsStack;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.keywords.handler.RenameKeywordsHandler.E4RenameKeywordsHandler;
import org.robotframework.viewers.Selections;

public class RenameKeywordsHandler extends DIHandler<E4RenameKeywordsHandler> {

    public RenameKeywordsHandler() {
        super(E4RenameKeywordsHandler.class);
    }

    public static class E4RenameKeywordsHandler {

        @Inject
        private RobotEditorCommandsStack commandsStack;

        @Execute
        public Object renameKeywords(@Named(Selections.SELECTION) final IStructuredSelection selection) {
            throw new RuntimeException("Not yet implemented!");
        }
    }
}
