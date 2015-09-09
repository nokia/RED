/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor;

import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.StyledString.Styler;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.TextStyle;
import org.eclipse.swt.widgets.Display;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.red.graphics.ColorsManager;
import org.robotframework.red.graphics.ImagesManager;

/**
 * Those objects are used in order to have additional entry in table which can
 * be handled by editing supports
 * 
 * @author anglart
 */
public class ElementAddingToken {

    private Color enabledColor = null;
    private Color disabledColor = null;
    private Font font = null;

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

    /**
     * Gets the image. It should be disposed later on manually or automatically
     * by framework.
     */
    public Image getImage() {
        if (rank > 0) {
            return null;
        }
        ImageDescriptor addImage = RedImages.getAddImage();
        return ImagesManager.getImage(enabled ? addImage : RedImages.getGreyedImage(addImage));
    }

    public StyledString getStyledText() {
        final String msg = rank == 0 ? "...add new" + " " + newElementTypeName : "...";
        return new StyledString(msg, new Styler() {
            @Override
            public void applyStyles(final TextStyle textStyle) {
                textStyle.foreground = getColor();
                final int style = rank == 0 ? SWT.ITALIC : SWT.ITALIC | SWT.BOLD;
                textStyle.font = getFont(textStyle.font, style);
            }
        });
    }

    private Color getColor() {
        if (enabled && enabledColor != null) {
            return enabledColor;
        } else if (enabled) {
            enabledColor = ColorsManager.getColor(30, 127, 60);
            return enabledColor;
        } else if (!enabled && disabledColor != null) {
            return disabledColor;
        } else if (!enabled) {
            disabledColor = ColorsManager.getColor(200, 200, 200);
            return disabledColor;
        }
        return null;
    }

    private Font getFont(final Font fontToReuse, final int style) {
        if (font != null) {
            return font;
        }
        final Font currentFont = fontToReuse == null ? Display.getCurrent().getSystemFont() : fontToReuse;
        final FontDescriptor fontDescriptor = FontDescriptor.createFrom(currentFont).setStyle(style);
        font = fontDescriptor.createFont(currentFont.getDevice());
        return font;
    }

    public void dispose() {
        if (enabledColor != null) {
            enabledColor.dispose();
        }
        if (disabledColor != null) {
            disabledColor.dispose();
        }
        if (font != null) {
            font.dispose();
        }
    }
}
