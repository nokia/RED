package org.robotframework.ide.eclipse.main.plugin;

import static com.google.common.collect.Lists.newArrayList;

import org.robotframework.ide.eclipse.main.plugin.RobotSetting.SettingsGroup;

public class RobotSuiteSettingsSection extends RobotSuiteFileSection {

    public static final String SECTION_NAME = "Settings";

    public RobotSuiteSettingsSection(final RobotSuiteFile parent, final boolean readOnly) {
        super(parent, SECTION_NAME, readOnly);
    }

    public RobotSetting createSetting(final String name, final String comment, final String... args) {
        RobotSetting setting;
        if (name.equals(SettingsGroup.METADATA.getName())) {
            setting = new RobotSetting(this, SettingsGroup.METADATA, name, newArrayList(args), comment);
        } else if (name.equals(SettingsGroup.LIBRARIES.getName())) {
            setting = new RobotSetting(this, SettingsGroup.LIBRARIES, name, newArrayList(args), comment);
        } else if (name.equals(SettingsGroup.RESOURCES.getName())) {
            setting = new RobotSetting(this, SettingsGroup.RESOURCES, name, newArrayList(args), comment);
        } else if (name.equals(SettingsGroup.VARIABLES.getName())) {
            setting = new RobotSetting(this, SettingsGroup.VARIABLES, name, newArrayList(args), comment);
        } else {
            setting = new RobotSetting(this, name, newArrayList(args), comment);
        }
        elements.add(setting);
        return setting;
    }
}
