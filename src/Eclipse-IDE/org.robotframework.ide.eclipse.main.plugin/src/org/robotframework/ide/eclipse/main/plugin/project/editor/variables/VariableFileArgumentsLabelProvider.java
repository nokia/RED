/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.editor.variables;

import static org.eclipse.jface.viewers.Stylers.withForeground;

import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.graphics.Image;
import org.rf.ide.core.project.RobotProjectConfig.ReferencedVariableFile;
import org.robotframework.red.viewers.RedCommonLabelProvider;

class VariableFileArgumentsLabelProvider extends RedCommonLabelProvider {
    
    private final int index;

    public VariableFileArgumentsLabelProvider(final int index) {
        this.index = index;
    }

    @Override
    public StyledString getStyledText(final Object element) {
        if (element instanceof ReferencedVariableFile) {
            final ReferencedVariableFile varFile = (ReferencedVariableFile) element;
            final String argument = index < varFile.getArguments().size() ? varFile.getArguments().get(index) : "";
            return new StyledString(argument, withForeground(150, 150, 150));
        } else {
            return new StyledString();
        }
    }

    @Override
    public Image getImage(final Object element) {
        return null;
    }
}
