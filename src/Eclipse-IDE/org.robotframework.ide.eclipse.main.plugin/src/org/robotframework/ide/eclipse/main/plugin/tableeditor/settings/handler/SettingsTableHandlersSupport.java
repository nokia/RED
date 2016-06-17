package org.robotframework.ide.eclipse.main.plugin.tableeditor.settings.handler;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.robotframework.ide.eclipse.main.plugin.model.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSetting;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSettingsSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSetting.SettingsGroup;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.settings.GeneralSettingsModel;

public class SettingsTableHandlersSupport {

    private SettingsTableHandlersSupport() {
    }

    public static List<RobotSetting> createSettingsCopy(final List<RobotSetting> settings) {
        final List<RobotSetting> settingsCopies = new ArrayList<>();
        for (final RobotSetting robotSetting : settings) {
            settingsCopies.add(new RobotSetting(null, robotSetting.getGroup(), new String(robotSetting.getName()),
                    new ArrayList<>(robotSetting.getArguments()), new String(robotSetting.getComment())));
        }
        return settingsCopies;
    }
    
    public static int findTableIndexOfSelectedSetting(final RobotSettingsSection section,
            final RobotSetting selectedSetting) {
        if (selectedSetting.getGroup() == SettingsGroup.METADATA) {
            return section.getMetadataSettings().indexOf(selectedSetting);
        } else if (selectedSetting.getGroup() == SettingsGroup.NO_GROUP) {
            final Iterator<RobotElement> generalSettingsIterator = GeneralSettingsModel.fillSettingsMapping(section)
                    .values()
                    .iterator();
            int i = 0;
            while (generalSettingsIterator.hasNext()) {
                if (selectedSetting.equals(generalSettingsIterator.next())) {
                    return i;
                }
                i++;
            }
        }
        return section.getImportSettings().indexOf(selectedSetting);
    }

}
