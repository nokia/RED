package org.robotframework.ide.eclipse.main.plugin;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;

import org.eclipse.core.runtime.IPath;
import org.robotframework.ide.eclipse.main.plugin.RobotSetting.SettingsGroup;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

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
    
    public List<RobotElement> getMetadataSettings() {
        return getSettingsFromGroup(SettingsGroup.METADATA);
    }

    public List<RobotElement> getResourcesSettings() {
        return getSettingsFromGroup(SettingsGroup.RESOURCES);
    }

    public List<RobotElement> getImportSettings() {
        return newArrayList(Iterables.filter(elements, new Predicate<RobotElement>() {
            @Override
            public boolean apply(final RobotElement element) {
                return SettingsGroup.getImportsGroupsSet()
                                .contains((((RobotSetting) element).getGroup()));
            }
        }));
    }

    private List<RobotElement> getSettingsFromGroup(final SettingsGroup group) {
        return newArrayList(Iterables.filter(elements, new Predicate<RobotElement>() {
            @Override
            public boolean apply(final RobotElement element) {
                return (((RobotSetting) element).getGroup() == group);
            }
        }));
    }

    public List<IPath> getResourcesPaths() {
        final List<RobotElement> resources = getResourcesSettings();
        final List<IPath> paths = newArrayList();
        for (final RobotElement element : resources) {
            final RobotSetting setting = (RobotSetting) element;
            final List<String> args = setting.getArguments();
            if (!args.isEmpty()) {
                paths.add(new org.eclipse.core.runtime.Path(args.get(0)));
            }
        }
        return paths;
    }
}
