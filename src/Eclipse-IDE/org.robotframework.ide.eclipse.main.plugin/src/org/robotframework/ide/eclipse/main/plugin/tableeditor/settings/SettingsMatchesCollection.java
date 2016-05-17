/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.settings;

import org.robotframework.ide.eclipse.main.plugin.model.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSetting;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSettingsSection;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.HeaderFilterMatchesCollection;

public class SettingsMatchesCollection extends HeaderFilterMatchesCollection {

    @Override
    public void collect(final RobotElement element, final String filter) {
        if (element instanceof RobotKeywordCall) {
            collectMatches((RobotKeywordCall) element, filter);
        } else if (element instanceof RobotSettingsSection) {
            for (final RobotKeywordCall setting : ((RobotSettingsSection) element).getChildren()) {
                collectMatches((RobotSetting) setting, filter);
            }
        }
    }

    private void collectMatches(final RobotKeywordCall setting, final String filter) {
        boolean isMatching = false; 

        isMatching |= collectMatches(filter, setting.getName());
        for (final String argument : setting.getArguments()) {
            isMatching |= collectMatches(filter, argument);
        }
        isMatching |= collectMatches(filter, setting.getComment());
        if (isMatching) {
            rowsMatching++;
        }
    }
}
