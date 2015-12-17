/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.editor.validation;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.Stylers.DisposeNeededStyler;
import org.eclipse.jface.viewers.StylersDisposingLabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.TextStyle;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.red.graphics.ColorsManager;
import org.robotframework.red.graphics.ImagesManager;

class ProjectValidationPathsLabelProvider extends StylersDisposingLabelProvider {
    
    @Override
    public StyledString getStyledText(final Object element) {
        final ProjectTreeElement projectTreeElement = (ProjectTreeElement) element;
        
        if (projectTreeElement.isExcluded()) {
            final DisposeNeededStyler styler = addDisposeNeededStyler(new DisposeNeededStyler() {

                @Override
                public void applyStyles(final TextStyle textStyle) {
                    textStyle.foreground = ColorsManager.getColor(220, 220, 220);

                }
            });
            return new StyledString(projectTreeElement.getLabel() + " [excluded]", styler);
        } else {
            return new StyledString(projectTreeElement.getLabel());
        }
    }

    @Override
    public Image getImage(final Object element) {
        final ProjectTreeElement projectTreeElement = (ProjectTreeElement) element;
        final ImageDescriptor imageDescriptor = projectTreeElement.getImageDescriptor();
        if (projectTreeElement.isExcluded()) {
            return ImagesManager.getImage(RedImages.getGreyedImage(imageDescriptor));
        } else {
            return ImagesManager.getImage(imageDescriptor);
        }
    }
}
