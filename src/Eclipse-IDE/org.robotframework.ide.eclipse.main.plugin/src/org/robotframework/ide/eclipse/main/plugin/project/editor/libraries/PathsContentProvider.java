/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.editor.libraries;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.rf.ide.core.SystemVariableAccessor;
import org.rf.ide.core.project.RobotProjectConfig.SearchPath;
import org.robotframework.red.viewers.ElementAddingToken;
import org.robotframework.red.viewers.StructuredContentProvider;

import com.google.common.annotations.VisibleForTesting;

/**
 * @author Michal Anglart
 *
 */
class PathsContentProvider extends StructuredContentProvider {

    private final String pathVariableName;

    private final boolean isEditable;

    private final SystemVariableAccessor variableAccessor;

    PathsContentProvider(final String pathVariableName, final boolean isEditable) {
        this(pathVariableName, isEditable, new SystemVariableAccessor());
    }

    @VisibleForTesting
    PathsContentProvider(final String pathVariableName, final boolean isEditable,
            final SystemVariableAccessor variableAccessor) {
        this.pathVariableName = pathVariableName;
        this.isEditable = isEditable;
        this.variableAccessor = variableAccessor;
    }

    @Override
    public Object[] getElements(final Object inputElement) {
        final List<SearchPath> pathsFromSystem = variableAccessor.getPaths(pathVariableName)
                .stream()
                .map(path -> SearchPath.create(path, true))
                .collect(Collectors.toList());

        final List<Object> elements = new ArrayList<>();
        elements.addAll(pathsFromSystem);
        elements.addAll((List<?>) inputElement);
        if (isEditable) {
            elements.add(new ElementAddingToken("search path", true));
        }

        return elements.toArray();
    }
}
