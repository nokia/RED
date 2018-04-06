/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.red.jface.preferences;

import java.io.File;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.variables.IStringVariableManager;
import org.eclipse.core.variables.VariablesPlugin;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import com.google.common.annotations.VisibleForTesting;

public class ParameterizedFilePathStringFieldEditor extends StringFieldEditor {

    public ParameterizedFilePathStringFieldEditor(final String name, final String labelText, final Composite parent) {
        super(name, labelText, parent);
    }

    @Override
    protected boolean checkState() {
        if (getTextControl() == null) {
            return false;
        }
        final String txt = getTextControl().getText();

        if (!txt.isEmpty()) {
            final IStringVariableManager variableManager = VariablesPlugin.getDefault().getStringVariableManager();
            try {
                final File file = new File(variableManager.performStringSubstitution(txt));
                if (!file.exists()) {
                    showErrorMessage(getErrorMessage());
                    return false;
                }
            } catch (final CoreException e) {
                showErrorMessage(getErrorMessage());
                return false;
            }
        }

        clearErrorMessage();
        return true;
    }

    @VisibleForTesting
    @Override
    protected Text getTextControl() {
        return super.getTextControl();
    }

    public void insertValue(final String value) {
        getTextControl().insert(value);
        valueChanged();
    }
}
