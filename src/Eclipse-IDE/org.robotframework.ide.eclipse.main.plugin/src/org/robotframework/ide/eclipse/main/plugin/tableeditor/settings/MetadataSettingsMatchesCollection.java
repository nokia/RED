/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.settings;

import org.robotframework.ide.eclipse.main.plugin.model.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSetting;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.HeaderFilterMatchesCollection;

class MetadataSettingsMatchesCollection extends HeaderFilterMatchesCollection {

    @Override
    public void collect(final RobotElement element, final String filter) {
        if (element instanceof RobotSetting) {
            collectMetadataMatches((RobotSetting) element, filter);
        }
    }

    private void collectMetadataMatches(final RobotSetting setting, final String filter) {
        boolean isMatching = false; 

        for (final String argument : setting.getArguments()) {
            isMatching |= collectMatches(filter, argument);
        }
        isMatching |= collectMatches(filter, setting.getComment());
        if (isMatching) {
            rowsMatching++;
        }
    }
}
