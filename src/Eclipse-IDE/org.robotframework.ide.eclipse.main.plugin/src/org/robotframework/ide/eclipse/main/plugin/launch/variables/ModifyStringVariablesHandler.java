/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.launch.variables;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.robotframework.ide.eclipse.main.plugin.launch.variables.ModifyStringVariablesHandler.E4ChangeActiveStringsSubstitutionVariables;
import org.robotframework.red.commands.DIParameterizedHandler;

public class ModifyStringVariablesHandler
        extends DIParameterizedHandler<E4ChangeActiveStringsSubstitutionVariables> {

    public ModifyStringVariablesHandler() {
        super(E4ChangeActiveStringsSubstitutionVariables.class);
    }

    public static class E4ChangeActiveStringsSubstitutionVariables {

        @Execute
        public void activate(final Display display) {
            final PreferenceDialog dialog = PreferencesUtil.createPreferenceDialogOn(display.getActiveShell(),
                    "org.robotframework.ide.eclipse.main.plugin.preferences.launch.activeVarsSets", null, null);
            dialog.open();
        }
    }
}
