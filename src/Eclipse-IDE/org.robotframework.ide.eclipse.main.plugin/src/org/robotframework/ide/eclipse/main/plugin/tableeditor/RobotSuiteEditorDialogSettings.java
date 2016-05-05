/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;

import com.google.common.base.Optional;

/**
 * @author Michal Anglart
 *
 */
public class RobotSuiteEditorDialogSettings {

    public boolean isHeaderFilteringEnabled() {
        final Optional<IDialogSettings> headerSettings = getSuiteEditorHeaderSettings();
        if (headerSettings.isPresent()) {
            return headerSettings.get().getBoolean("filterEnabled");
        }
        return true;
    }

    public void setHeaderFilteringEnabled(final boolean enabled) {
        Optional<IDialogSettings> headerSettings = getSuiteEditorHeaderSettings();
        if (!headerSettings.isPresent()) {
            Optional<IDialogSettings> editorSettings = getSuiteEditorHeaderSettings();
            if (!editorSettings.isPresent()) {
                final IDialogSettings settings = RedPlugin.getDefault().getDialogSettings();
                editorSettings = Optional.of(settings.addNewSection(RobotFormEditor.ID));
                headerSettings = Optional.of(editorSettings.get().addNewSection("header"));
            }
        }
        headerSettings.get().put("filterEnabled", enabled);
    }

    private Optional<IDialogSettings> getSuiteEditorSettings() {
        final IDialogSettings dialogSettings = RedPlugin.getDefault().getDialogSettings();
        return Optional.fromNullable(dialogSettings.getSection(RobotFormEditor.ID));
    }

    private Optional<IDialogSettings> getSuiteEditorHeaderSettings() {
        final Optional<IDialogSettings> editorSettings = getSuiteEditorSettings();
        if (editorSettings.isPresent()) {
            return Optional.fromNullable(editorSettings.get().getSection("header"));
        }
        return Optional.absent();
    }
}
