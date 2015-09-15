/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.settings;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSettingsSection;

class GeneralSettingsContentProvider implements IStructuredContentProvider {

    @Override
    public void dispose() {
        // nothing to do
    }

    @Override
    public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput) {
        // nothing to do
    }

    @Override
    public Object[] getElements(final Object inputElement) {
        final RobotSettingsSection section = (RobotSettingsSection) inputElement;
        if (section != null) {
            return GeneralSettingsModel.fillSettingsMapping(section).entrySet().toArray();
        }
        return new Object[0];
    }

}
