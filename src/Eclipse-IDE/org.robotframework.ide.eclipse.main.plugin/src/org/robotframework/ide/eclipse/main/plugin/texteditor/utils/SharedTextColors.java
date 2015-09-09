/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see licence.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.texteditor.utils;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.text.source.ISharedTextColors;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

public class SharedTextColors implements ISharedTextColors {

    private final Map<RGB, Color> colors = new HashMap<>();

	@Override
	public Color getColor(final RGB rgb) {
        Color color = colors.get(rgb);
        if (color == null) {
            final Display display = Display.getCurrent();
            color = new Color(display, rgb);
            colors.put(rgb, color);
        }
        return color;
	}

	@Override
	public void dispose() {
        for (final Color color : colors.values()) {
            color.dispose();
        }
        colors.clear();
	}
}
