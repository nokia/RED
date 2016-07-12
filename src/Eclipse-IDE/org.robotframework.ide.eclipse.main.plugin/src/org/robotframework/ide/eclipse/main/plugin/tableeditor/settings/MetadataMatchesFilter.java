/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.settings;

import java.util.List;

import org.robotframework.ide.eclipse.main.plugin.model.RobotSetting;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.HeaderFilterMatchesCollection;

public class MetadataMatchesFilter {

    private final HeaderFilterMatchesCollection matches;

    public MetadataMatchesFilter(final HeaderFilterMatchesCollection matches) {
        this.matches = matches;
    }

    public boolean isMatching(final Object element) {
        if (element instanceof RobotSetting) {
            return metadataSettingMatches((RobotSetting) element);
        }
        return true;
    }

    private boolean metadataSettingMatches(final RobotSetting setting) {
        return matches.contains(setting.getComment()) || matchesContainsArgument(setting.getArguments());
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
