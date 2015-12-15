/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.editor.variables;

import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.StyledString.Styler;
import org.eclipse.jface.viewers.Stylers.DisposeNeededStyler;
import org.eclipse.jface.viewers.StylersDisposingLabelProvider;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.TextStyle;
import org.eclipse.swt.widgets.Display;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfig.ReferencedVariableFile;

class VariableFileArgumentsLabelProvider extends StylersDisposingLabelProvider {
    
    private final int index;

    public VariableFileArgumentsLabelProvider(final int index) {
        this.index = index;
    }

    @Override
    public StyledString getStyledText(final Object element) {
        if (element instanceof ReferencedVariableFile) {
            final ReferencedVariableFile varFile = (ReferencedVariableFile) element;
            final Styler styler = addDisposeNeededStyler(new DisposeNeededStyler() {
                @Override
                public void applyStyles(final TextStyle textStyle) {
                    textStyle.foreground = new Color(Display.getCurrent(), 150, 150, 150);
                }
            });
            final String argument = index < varFile.getArguments().size() ? varFile.getArguments().get(index) : "";
            return new StyledString(argument, styler);
        } else {
            return new StyledString();
        }
    }

    @Override
    public Image getImage(final Object element) {
        return null;
    }
}
