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

class MetadataSettingsContentProvider extends StructuredContentProvider {

    private final boolean editable;

    public MetadataSettingsContentProvider(final boolean editable) {
        this.editable = editable;
    }

    @Override
    public Object[] getElements(final Object inputElement) {
        final RobotSettingsSection section = (RobotSettingsSection) inputElement;
        final Object[] elements = getMetadataElements(section).toArray();
        final Object[] newElements = Arrays.copyOf(elements, elements.length + 1, Object[].class);
        final ElementAddingToken elementAddingToken = new ElementAddingToken("metadata", editable);
        newElements[elements.length] = elementAddingToken;
        return newElements;
    }

    private List<RobotKeywordCall> getMetadataElements(final RobotSettingsSection section) {
        return section != null ? section.getMetadataSettings() : new ArrayList<RobotKeywordCall>();
    }
}
