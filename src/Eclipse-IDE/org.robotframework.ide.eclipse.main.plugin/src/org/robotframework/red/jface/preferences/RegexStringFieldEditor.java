/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.red.jface.preferences;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import com.google.common.annotations.VisibleForTesting;

public class RegexStringFieldEditor extends StringFieldEditor {

    public RegexStringFieldEditor(final String name, final String labelText, final Composite parent) {
        super(name, labelText, parent);
    }

    @Override
    protected boolean checkState() {
        if (getTextControl() == null) {
            return false;
        }

        if (!super.checkState()) {
            showErrorMessage(getErrorMessage());
            return false;
        }

        try {
            Pattern.compile(getTextControl().getText());
        } catch (final PatternSyntaxException e) {
            final String locMessage = e.getLocalizedMessage();
            int i = 0;
            while (i < locMessage.length() && "\n\r".indexOf(locMessage.charAt(i)) == -1) { //$NON-NLS-1$
                i++;
            }
            showErrorMessage(locMessage);
            return false;
        }
        clearErrorMessage();
        return true;
    }

    @VisibleForTesting
    @Override
    protected Text getTextControl() {
        return super.getTextControl();
    }
}
