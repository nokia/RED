/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.editor.variables;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Path;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.StyledString.Styler;
import org.eclipse.jface.viewers.Stylers.DisposeNeededStyler;
import org.eclipse.jface.viewers.StylersDisposingLabelProvider;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.TextStyle;
import org.eclipse.swt.widgets.Display;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.RedTheme;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfig.ReferencedVariableFile;
import org.robotframework.red.graphics.ImagesManager;

import com.google.common.base.Joiner;

class VariableFilesLabelProvider extends StylersDisposingLabelProvider {
    
    @Override
    public StyledString getStyledText(final Object element) {
        final ReferencedVariableFile varFile = (ReferencedVariableFile) element;

        final StyledString label = new StyledString(Path.fromPortableString(varFile.getPath()).lastSegment());

        final List<String> args = varFile.getArguments() != null ? varFile.getArguments() : new ArrayList<String>();
        if (!args.isEmpty()) {
            label.append(" (" + Joiner.on(", ").join(args) + ")", addDisposeNeededStyler(new DisposeNeededStyler() {

                @Override
                public void applyStyles(final TextStyle textStyle) {
                    textStyle.foreground = new Color(Display.getCurrent(), 200, 200, 200);
                    markForDisposal(textStyle.foreground);
                }
            }));
        }
        label.append(" - " + new Path(varFile.getPath()), new Styler() {

            @Override
            public void applyStyles(final TextStyle textStyle) {
                textStyle.foreground = RedTheme.getEclipseDecorationColor();
            }
        });
        return label;

    }

    @Override
    public Image getImage(final Object element) {
        return ImagesManager.getImage(RedImages.getRobotScalarVariableImage());
    }
}
