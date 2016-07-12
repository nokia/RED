/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.settings;

import java.util.List;
import java.util.Map.Entry;

import org.robotframework.ide.eclipse.main.plugin.model.RobotDefinitionSetting;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSetting;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.HeaderFilterMatchesCollection;

public class SettingsMatchesFilter {

    private final HeaderFilterMatchesCollection matches;

    public SettingsMatchesFilter(final HeaderFilterMatchesCollection matches) {
        this.matches = matches;
    }

    public boolean isMatching(final Object element) {
        if (element instanceof RobotSetting) {
            return settingMatches((RobotSetting) element);
        } else if (element instanceof Entry<?, ?>) {
            final Object elem = ((Entry<?, ?>) element).getValue();
            if (elem instanceof RobotSetting) {
                return settingMatches((RobotSetting) elem);
            } else if (elem instanceof RobotDefinitionSetting) {
                return settingMatches((RobotDefinitionSetting) elem);
            }
        }
        return true;
    }

    private boolean settingMatches(final RobotKeywordCall setting) {
        return matches.contains(setting.getName()) || matches.contains(setting.getComment())
                || matchesContainsArgument(setting.getArguments());
    }

    private boolean matchesContainsArgument(final List<String> arguments) {
        for (final String argument : arguments) {
            if (matches.contains(argument)) {
                return true;
            }
        }
        return false;
    }
}
