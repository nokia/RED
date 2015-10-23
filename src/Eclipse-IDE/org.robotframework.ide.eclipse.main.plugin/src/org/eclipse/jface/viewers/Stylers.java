/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.eclipse.jface.viewers;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.jface.viewers.StyledString.Styler;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Resource;
import org.eclipse.swt.graphics.TextStyle;
import org.eclipse.swt.widgets.Display;
import org.robotframework.red.graphics.ColorsManager;

public class Stylers {

    public static class Common {

        public static Styler MARKED_PREFIX_STYLER = new Styler() {
            @Override
            public void applyStyles(final TextStyle style) {
                style.foreground = ColorsManager.getColor(135, 150, 255);
                style.background = ColorsManager.getColor(230, 240, 255);
                style.borderColor = ColorsManager.getColor(135, 150, 255);
                style.borderStyle = SWT.BORDER_DOT;
            }
        };
    }

    public static DisposeNeededStyler mixStylers(final Styler... stylers) {
        return new DisposeNeededStyler() {
            @Override
            public void applyStyles(final TextStyle textStyle) {
                for (final Styler styler : stylers) {
                    styler.applyStyles(textStyle);

                    if (styler instanceof DisposeNeededStyler) {
                        for (final Resource resource : ((DisposeNeededStyler) styler).getResourceHandles()) {
                            markForDisposal(resource);
                        }
                    }
                }
            }
        };
    }

    public static DisposeNeededStyler withForeground(final int red, final int green, final int blue) {
        return new DisposeNeededStyler() {
            @Override
            public void applyStyles(final TextStyle textStyle) {
                textStyle.foreground = new Color(Display.getCurrent(), red, green, blue);
                markForDisposal(textStyle.foreground);
            }
        };
    }

    public static DisposeNeededStyler withForeground(final RGB rgb) {
        return new DisposeNeededStyler() {
            @Override
            public void applyStyles(final TextStyle textStyle) {
                textStyle.foreground = new Color(Display.getCurrent(), rgb);
                markForDisposal(textStyle.foreground);
            }
        };
    }

    public static DisposeNeededStyler withFontStyle(final int style) {
        return new DisposeNeededStyler() {
            @Override
            public void applyStyles(final TextStyle textStyle) {
                final Font currentFont = textStyle.font == null ? Display.getCurrent().getSystemFont() : textStyle.font;
                FontDescriptor fontDescriptor = FontDescriptor.createFrom(currentFont);
                fontDescriptor = fontDescriptor.setStyle(style);

                textStyle.font = fontDescriptor.createFont(currentFont.getDevice());
                markForDisposal(textStyle.font);
            }
        };
    }

    public abstract static class DisposeNeededStyler extends Styler {
        private final List<Resource> resourceHandles = new ArrayList<Resource>();

        private List<Resource> getResourceHandles() {
            return resourceHandles;
        }

        public void markForDisposal(final Resource resource) {
            resourceHandles.add(resource);
        }

        public void dispose() {
            for (final Resource resource : resourceHandles) {
                resource.dispose();
            }
            resourceHandles.clear();
        }
    }
}
