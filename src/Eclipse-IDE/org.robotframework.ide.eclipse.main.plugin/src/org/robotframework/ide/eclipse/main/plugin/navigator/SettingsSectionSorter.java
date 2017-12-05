/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.navigator;

import org.robotframework.ide.eclipse.main.plugin.model.RobotSetting.SettingsGroup;

public class SettingsSectionSorter extends RobotElementsSorter {

    @Override
    public int category(final Object element) {
        if (element instanceof ArtificialGroupingRobotElement) {
            final ArtificialGroupingRobotElement el = (ArtificialGroupingRobotElement) element;
            final SettingsGroup group = el.getGroup();
            return SettingsGroup.getImportsGroupsSet().contains(group) ? 2 : 1;
        }
        return 0;
    }
}
