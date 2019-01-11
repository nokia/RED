/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.launch.variables;

import java.util.Map;
import java.util.Optional;

import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.menus.UIElement;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.RedPreferences;
import org.robotframework.ide.eclipse.main.plugin.launch.variables.ActivateStringVariablesHandler.E4ActivateStringVariablesHandler;
import org.robotframework.red.commands.DIParameterizedHandler;

public class ActivateStringVariablesHandler
        extends DIParameterizedHandler<E4ActivateStringVariablesHandler> implements IElementUpdater {

    public ActivateStringVariablesHandler() {
        super(E4ActivateStringVariablesHandler.class);
    }

    @Override
    public void updateElement(final UIElement element, @SuppressWarnings("rawtypes") final Map parameters) {
        final String setName = (String) parameters
                .get(ActivateStringVariablesMenuItem.ACTIVATE_SET_COMMAND_PARAMETER_ID);
        final Optional<String> active = currentlyActive();
        if (active.isPresent() && active.get().equals(setName) || !active.isPresent() && setName.isEmpty()) {
            element.setChecked(true);
        }
    }

    private static Optional<String> currentlyActive() {
        return RedPlugin.getDefault().getPreferences().getActiveVariablesSet();
    }

    public static class E4ActivateStringVariablesHandler {

        @Execute
        public void activate(@Named(ActivateStringVariablesMenuItem.ACTIVATE_SET_COMMAND_PARAMETER_ID) final String setName) {
            final String active = currentlyActive().orElse("");
            if (!active.equals(setName)) {
                RedPlugin.getDefault()
                        .getPreferenceStore()
                        .setValue(RedPreferences.STRING_VARIABLES_ACTIVE_SET, setName);
            }
        }
    }
}
