/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.editor;

import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.StyledString.Styler;
import org.eclipse.jface.viewers.StylersDisposingLabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.TextStyle;
import org.robotframework.ide.eclipse.main.plugin.RedTheme;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfig.ReferencedLibrary;
import org.robotframework.red.graphics.ImagesManager;

class ReferencedLibrariesLabelProvider extends StylersDisposingLabelProvider {

    @Override
    public StyledString getStyledText(final Object element) {
        final ReferencedLibrary lib = (ReferencedLibrary) element;

        final StyledString label = new StyledString(lib.getName());
        label.append(" - " + lib.getPath(), new Styler() {

            @Override
            public void applyStyles(final TextStyle textStyle) {
                textStyle.foreground = RedTheme.getEclipseDecorationColor();
            }
        });
        return label;
    }

    @Override
    public Image getImage(final Object element) {
        final ReferencedLibrary library = (ReferencedLibrary) element;
        return ImagesManager.getImage(library.getImage());
    }
}
