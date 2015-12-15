/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.settings;

import org.robotframework.ide.eclipse.main.plugin.model.RobotSettingsSection;
import org.robotframework.red.viewers.StructuredContentProvider;

class GeneralSettingsContentProvider extends StructuredContentProvider {

    @Override
    public Object[] getElements(final Object inputElement) {
        final RobotSettingsSection section = (RobotSettingsSection) inputElement;
        if (section != null) {
            return GeneralSettingsModel.fillSettingsMapping(section).entrySet().toArray();
        }
        return new Object[0];
    }

}
