package org.robotframework.ide.eclipse.main.plugin;

import java.util.EnumSet;
import java.util.List;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IWorkbenchPage;

public class RobotSetting extends RobotKeywordCall {

    private final SettingsGroup group;

    RobotSetting(final RobotSuiteSettingsSection section, final String name, final List<String> args,
            final String comment) {
        this(section, SettingsGroup.NO_GROUP, name, args, comment);
    }

    RobotSetting(final RobotSuiteSettingsSection section, final SettingsGroup group, final String name,
            final List<String> args, final String comment) {
        super(section, name, args, comment);
        this.group = group;
    }

    public SettingsGroup getGroup() {
        return group;
    }

    public String getNameInGroup() {
        final List<String> arguments = getArguments();
        return arguments.isEmpty() ? "" : arguments.get(0);
    }

    @Override
    public ImageDescriptor getImage() {
        return RobotImages.getRobotSettingImage();
    }

    @Override
    public OpenStrategy getOpenRobotEditorStrategy(final IWorkbenchPage page) {
        return new PageActivatingOpeningStrategy(page, getSuiteFile().getFile(), (RobotSuiteFileSection) getParent(),
                this);
    }

    public enum SettingsGroup {
        NO_GROUP {
            @Override
            public String getName() {
                return null;
            }
        },
        METADATA {
            @Override
            public String getName() {
                return "Metadata";
            }
        },
        LIBRARIES {
            @Override
            public String getName() {
                return "Library";
            }
        },
        RESOURCES {
            @Override
            public String getName() {
                return "Resource";
            }
        },
        VARIABLES {
            @Override
            public String getName() {
                return "Variables";
            }
        };

        public static EnumSet<SettingsGroup> getImportsGroupsSet() {
            return EnumSet.of(LIBRARIES, RESOURCES, VARIABLES);
        }

        public abstract String getName();
    }
}
