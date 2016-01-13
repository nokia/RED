/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.settings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSettingsSection;
import org.robotframework.red.viewers.ElementAddingToken;
import org.robotframework.red.viewers.StructuredContentProvider;

class ImportSettingsContentProvider extends StructuredContentProvider {

    private final boolean editable;

    ImportSettingsContentProvider(final boolean editable) {
        this.editable = editable;
    }

    @Override
    public Object[] getElements(final Object inputElement) {
        final RobotSettingsSection section = (RobotSettingsSection) inputElement;
        final Object[] elements = getImportElements(section).toArray();
        final Object[] newElements = Arrays.copyOf(elements, elements.length + 1, Object[].class);
        final ElementAddingToken elementAddingToken = new ElementAddingToken("import", editable);
        newElements[elements.length] = elementAddingToken;
        return newElements;
    }

    private List<RobotKeywordCall> getImportElements(final RobotSettingsSection section) {
        return section != null ? section.getImportSettings() : new ArrayList<RobotKeywordCall>();
    }
}
