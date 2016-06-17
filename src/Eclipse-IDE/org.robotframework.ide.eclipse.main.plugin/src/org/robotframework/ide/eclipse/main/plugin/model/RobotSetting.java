/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model;

import java.util.EnumSet;
import java.util.List;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IWorkbenchPage;
import org.robotframework.ide.eclipse.main.plugin.RedImages;

public class RobotSetting extends RobotKeywordCall {

    private final SettingsGroup group;

    RobotSetting(final RobotSettingsSection section, final String name, final List<String> args,
            final String comment) {
        this(section, SettingsGroup.NO_GROUP, name, args, comment);
    }

    public RobotSetting(final RobotSettingsSection section, final SettingsGroup group, final String name,
            final List<String> args, final String comment) {
        super(section, name, args, comment);
        this.group = group;
    }

    @Override
    public RobotSettingsSection getParent() {
        return (RobotSettingsSection) super.getParent();
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
        return RedImages.getRobotSettingImage();
    }

    @Override
    public OpenStrategy getOpenRobotEditorStrategy(final IWorkbenchPage page) {
        return new PageActivatingOpeningStrategy(page, getSuiteFile().getFile(), getParent(),
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
