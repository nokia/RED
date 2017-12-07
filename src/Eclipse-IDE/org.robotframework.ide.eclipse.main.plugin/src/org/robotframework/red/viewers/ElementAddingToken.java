/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.red.viewers;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.StyledString.Styler;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.TextStyle;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.red.graphics.ColorsManager;
import org.robotframework.red.graphics.FontsManager;
import org.robotframework.red.graphics.ImagesManager;

/**
 * Those objects are used in order to have additional entry in table which can
 * be handled by editing supports
 * 
 * @author anglart
 */
public class ElementAddingToken {

    private final String newElementTypeName;
    private final boolean enabled;
    private final int rank;
    private final Object parent;

    public ElementAddingToken(final String newElementTypeName, final boolean isEnabled) {
        this(null, newElementTypeName, isEnabled, 0);
    }

    public ElementAddingToken(final Object parent, final String newElementTypeName, final boolean isEnabled,
            final int rank) {
        this.parent = parent;
        this.newElementTypeName = newElementTypeName;
        this.enabled = isEnabled;
        this.rank = rank;
    }

    public Object getParent() {
        return parent;
    }

    public Image getImage() {
        if (rank > 0) {
            return null;
        }
        final ImageDescriptor addImage = RedImages.getAddImage();
        return ImagesManager.getImage(enabled ? addImage : RedImages.getGrayedImage(addImage));
    }

    public StyledString getStyledText() {
        final String msg = rank == 0 ? "...add new" + " " + newElementTypeName : "...";
        return new StyledString(msg, new Styler() {
            @Override
            public void applyStyles(final TextStyle textStyle) {
                textStyle.foreground = enabled ? ColorsManager.getColor(30, 127, 60) : ColorsManager.getColor(200, 200, 200);
                textStyle.font = FontsManager.transformFontWithStyle(textStyle.font,
                        rank == 0 ? SWT.ITALIC : SWT.ITALIC | SWT.BOLD);
            }
        });
    }
}
