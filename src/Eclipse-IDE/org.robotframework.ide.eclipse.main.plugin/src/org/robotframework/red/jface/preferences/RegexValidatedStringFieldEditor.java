/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.red.jface.preferences;

import java.util.regex.Pattern;

import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;


/**
 * @author Michal Anglart
 *
 */
public class RegexValidatedStringFieldEditor extends StringFieldEditor {

    private final Pattern pattern;

    public RegexValidatedStringFieldEditor(final String name, final String labelText, final String regex,
            final Composite composite) {
        super(name, labelText, composite);
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

    @Override
    protected Text getTextControl() { // making it available for tests plugin
        return super.getTextControl();
    }
}
