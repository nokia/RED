/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.editor.libraries;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Lists.transform;

import java.io.File;
import java.util.List;

import org.rf.ide.core.project.RobotProjectConfig.SearchPath;
import org.robotframework.red.viewers.ElementAddingToken;
import org.robotframework.red.viewers.StructuredContentProvider;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;

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

        final List<String> paths = variableAccessor.getPaths(pathVariableName);

        final List<Object> elements = newArrayList();
        elements.addAll(transform(paths, path -> SearchPath.create(path, true)));
        elements.addAll((List<?>) inputElement);
        if (isEditable) {
            elements.add(new ElementAddingToken("search path", true));
        }

        return elements.toArray();
    }

    static class SystemVariableAccessor {
        List<String> getPaths(final String variableName) {
            return Splitter.on(File.pathSeparatorChar)
                    .omitEmptyStrings()
                    .splitToList(Strings.nullToEmpty(System.getenv(variableName)));
        }
    }
}
