package org.robotframework.ide.eclipse.main.plugin.tableeditor.settings.handler;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.tools.compat.parts.DIHandler;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.robotframework.ide.eclipse.main.plugin.RobotSetting;
import org.robotframework.ide.eclipse.main.plugin.cmd.DeleteSettingKeywordCallCommand;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorCommandsStack;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.settings.handler.DeleteSettingsHandler.E4DeleteSettingsHandler;
import org.robotframework.viewers.Selections;

public class DeleteSettingsHandler extends DIHandler<E4DeleteSettingsHandler> {

    public DeleteSettingsHandler() {
        super(E4DeleteSettingsHandler.class);
    }

    public static class E4DeleteSettingsHandler {

        @Inject
        private RobotEditorCommandsStack commandsStack;

        @Execute
        public Object deleteSettings(@Named(Selections.SELECTION) final IStructuredSelection selection) {
            final List<RobotSetting> settings = Selections.getElements(selection, RobotSetting.class);
            commandsStack.execute(new DeleteSettingKeywordCallCommand(settings));

            return null;
        }
    }
}
