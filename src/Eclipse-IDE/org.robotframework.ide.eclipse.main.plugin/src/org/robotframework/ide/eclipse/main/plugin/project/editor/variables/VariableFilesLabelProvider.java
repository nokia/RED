/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.editor.variables;

import org.eclipse.core.runtime.Path;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.StyledString.Styler;
import org.eclipse.jface.viewers.StylersDisposingLabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.TextStyle;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.RedTheme;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfig.ReferencedVariableFile;
import org.robotframework.red.graphics.ImagesManager;
import org.robotframework.red.viewers.ElementAddingToken;

class VariableFilesLabelProvider extends StylersDisposingLabelProvider {
    
    @Override
    public StyledString getStyledText(final Object element) {
        if (element instanceof ReferencedVariableFile) {
            return getStyledText((ReferencedVariableFile) element);
        } else if (element instanceof ElementAddingToken) {
            return ((ElementAddingToken) element).getStyledText();
        } else {
            return new StyledString();
        }
    }

    private StyledString getStyledText(final ReferencedVariableFile element) {
        final ReferencedVariableFile varFile = element;
        final StyledString label = new StyledString(Path.fromPortableString(varFile.getPath()).lastSegment());
        label.append(' ');
        label.append("- " + new Path(varFile.getPath()), new Styler() {
            @Override
            public void applyStyles(final TextStyle textStyle) {
                textStyle.foreground = RedTheme.getEclipseDecorationColor();
            }
        });
        return label;

    }

    @Override
    public Image getImage(final Object element) {
        if (element instanceof ElementAddingToken) {
            return ((ElementAddingToken) element).getImage();
        } else {
            return ImagesManager.getImage(RedImages.getRobotScalarVariableImage());
        }
    }
}
