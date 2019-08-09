/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.red.nattable.edit;

import java.util.List;
import java.util.Map.Entry;

import org.eclipse.nebula.widgets.nattable.data.IRowDataProvider;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;

/**
 * @author Michal Anglart
 */
public class DefaultRedCellEditorValueValidator implements CellEditorValueValidator<String> {

    private final IRowDataProvider<?> dataProvider;

    public DefaultRedCellEditorValueValidator(final IRowDataProvider<?> dataProvider) {
        this.dataProvider = dataProvider;
    }

    @Override
    public void validate(final String value, final int rowId) {
        if (value == null) {
            return;
        }
        Object robotObject = dataProvider.getRowObject(rowId);
        if (robotObject instanceof Entry) {
            robotObject = ((Entry<?, ?>) robotObject).getValue();
        }
        if (robotObject instanceof RobotKeywordCall) {
            RobotKeywordCall call = (RobotKeywordCall) robotObject;
            int offset = call.getPosition().getOffset();
            if (offset > -1) {
                List<String> separator = call.getSuiteFile()
                        .getLinkedElement()
                        .getRobotLineBy(offset)
                        .get()
                        .getSeparatorForLine()
                        .get()
                        .getRepresentation();
                for (String representation : separator) {
                    if (value.contains(representation)) {
                        throw new CellEditorValueValidationException("Single entry cannot contain cells separator");
                    }
                }
            }
        } else {
            String userSeparator = RedPlugin.getDefault().getPreferences().getSeparatorToUse(false);
            if (userSeparator.contains("|")) {
                userSeparator = "( |\t)\\|( |\t)";
            } else {
                userSeparator = "(  |\t)";
            }
            if (value.matches(".*" + userSeparator + ".*")) {
                throw new CellEditorValueValidationException("Single entry cannot contain cells separator");
            }
        }

        if (value.startsWith(" ") || (value.endsWith(" ") && !value.endsWith("\\ "))) {
            throw new CellEditorValueValidationException("Space should be escaped.");
        }
    }
}
