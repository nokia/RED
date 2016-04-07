/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.editor.libraries;

import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.Stylers;
import org.eclipse.swt.graphics.Image;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfig.SearchPath;
import org.robotframework.red.viewers.ElementAddingToken;
import org.robotframework.red.viewers.RedCommonLabelProvider;


/**
 * @author Michal Anglart
 *
 */
class PathsLabelProvider extends RedCommonLabelProvider {

    private final String pathVariableName;

    public PathsLabelProvider(final String pathVariableName) {
        this.pathVariableName = pathVariableName;
    }

    @Override
    public StyledString getStyledText(final Object element) {
        if (element instanceof SearchPath) {
            final SearchPath searchPath = (SearchPath) element;
            if (searchPath.isSystem()) {
                final StyledString label = new StyledString(searchPath.getPath(),
                        Stylers.withForeground(150, 150, 150));
                label.append(" [already defined in " + pathVariableName + " variable]",
                        Stylers.Common.ECLIPSE_DECORATION_STYLER);
                return label;
            } else {
                return new StyledString(searchPath.getPath());
            }
        } else {
            return ((ElementAddingToken) element).getStyledText();
        }
    }

    @Override
    public Image getImage(final Object element) {
        if (element instanceof ElementAddingToken) {
            return ((ElementAddingToken) element).getImage();
        }
        return null;
    }
}
