/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.red.jface.preferences;

import java.util.regex.Pattern;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import com.google.common.annotations.VisibleForTesting;

public class RegexValidatedMultilineStringFieldEditor extends MultiLineStringFieldEditor {

    private final Pattern pattern;

    public RegexValidatedMultilineStringFieldEditor(final String name, final String labelText, final int widthInChars,
            final int heigthInChars, final int strategy, final String regex, final Composite parent) {
        super(name, labelText, widthInChars, heigthInChars, strategy, parent);
        this.pattern = Pattern.compile(regex);
    }

    @Override
    protected boolean checkState() {
        if (getTextControl() == null) {
            return false;
        }
        final String txt = getTextControl().getText();

        if (pattern.matcher(txt).matches()) {
            clearErrorMessage();
            return true;
        } else {
            showErrorMessage(getErrorMessage());
            return false;
        }
    }

    @VisibleForTesting
    @Override
    protected Text getTextControl() {
        return super.getTextControl();
    }
}
