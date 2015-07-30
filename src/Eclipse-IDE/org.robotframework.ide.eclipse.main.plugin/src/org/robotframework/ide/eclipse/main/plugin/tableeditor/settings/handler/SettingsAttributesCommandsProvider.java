package org.robotframework.ide.eclipse.main.plugin.tableeditor.settings.handler;

import org.robotframework.ide.eclipse.main.plugin.model.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSetting;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSetting.SettingsGroup;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.SetKeywordCallArgumentCommand;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.SetKeywordCallCommentCommand;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;

import com.google.common.base.Optional;

class SettingsAttributesCommandsProvider {

    Optional<? extends EditorCommand> provide(final RobotElement element, final int index, final int noOfColumns, final String attribute) {
        if (!(element instanceof RobotSetting)) {
            return Optional.absent();
        }
        final RobotSetting setting = (RobotSetting) element;
        if (SettingsGroup.getImportsGroupsSet().contains(setting.getGroup())) {
            // some kind of import
            if (index == 0) {
                // don't want to do anything
                return Optional.absent();
            } else if (index == noOfColumns - 1) {
                return Optional.of(new SetKeywordCallCommentCommand(setting, attribute));
            } else {
                return Optional.of(new SetKeywordCallArgumentCommand(setting, index - 1, attribute));
            }
        } else if (setting.getGroup() == SettingsGroup.METADATA) {
            // metadata setting
            if (index == noOfColumns - 1) {
                return Optional.of(new SetKeywordCallCommentCommand(setting, attribute));
            } else {
                return Optional.of(new SetKeywordCallArgumentCommand(setting, index, attribute));
            }
        } else {
            // general setting
            if (index == 0) {
                // don't want to do anything
                return Optional.absent();
            } else if (index == noOfColumns - 1) {
                return Optional.of(new SetKeywordCallCommentCommand(setting, attribute));
            } else {
                return Optional.of(new SetKeywordCallArgumentCommand(setting, index - 1, attribute));
            }
        }
    }

}
