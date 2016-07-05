/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.navigator;

import java.text.Collator;

import org.robotframework.ide.eclipse.main.plugin.model.RobotSetting.SettingsGroup;

public class SettingsSectionSorter extends RobotElementsSorter {

    public SettingsSectionSorter() {
        // nothing to do
    }

    public SettingsSectionSorter(final Collator collator) {
        super(collator);
    }

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
