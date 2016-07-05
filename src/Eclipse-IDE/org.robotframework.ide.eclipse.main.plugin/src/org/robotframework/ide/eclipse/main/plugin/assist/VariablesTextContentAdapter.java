/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.assist;

import org.eclipse.jface.fieldassist.TextContentAdapter;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;


/**
 * @author Michal Anglart
 *
 */
public class VariablesTextContentAdapter extends TextContentAdapter {

    @Override
    public void insertControlContents(final Control control, final String text, final int cursorPosition) {
        final Text textControl = (Text) control;
        final String currentTextBeforeSelection = textControl.getText().substring(0, textControl.getSelection().x);
        final String currentTextAfterSelection = textControl.getText().substring(textControl.getSelection().x);

        int maxCommon = 0;
        for (int i = 0; i < text.length() && i <= currentTextBeforeSelection.length(); i++) {
            final String currentSuffix = currentTextBeforeSelection.substring(currentTextBeforeSelection.length() - i,
                    currentTextBeforeSelection.length());
            final String toInsertPrefix = text.substring(0, i);

            if (currentSuffix.equalsIgnoreCase(toInsertPrefix)) {
                maxCommon = toInsertPrefix.length();
            }
        }

        String toInsert = currentTextBeforeSelection.substring(0,
                currentTextBeforeSelection.length() - maxCommon) + text;
        final int newSelection = toInsert.length();
        toInsert += currentTextAfterSelection;

        textControl.setText(toInsert);
        textControl.setSelection(newSelection);
    }
}
