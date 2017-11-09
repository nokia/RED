/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.red.graphics;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

public class ColorsManager {
    private static final Map<RGB, Color> COLOR_TABLE = new HashMap<>(10);

    private ColorsManager() {
        // nothing to do
    }

    public static int size() {
        return COLOR_TABLE.size();
    }

    /**
     * Gets color for given system color ID (taken from SWT class). Can be
     * called only from GUI thread
     * 
     * @param systemColorID
     * @return Color instance
     */
    public static Color getColor(final int systemColorID) {
        return getColor(Display.getCurrent(), systemColorID);
    }

    /**
     * Gets color for given system color ID (taken from SWT class)
     * 
     * @param display
     * @param systemColorID
     * @return Color instance
     */
    public static Color getColor(final Display display, final int systemColorID) {
        return display.getSystemColor(systemColorID);
    }

    /**
     * Get color based on red, green and blue. Can be called only from GUI
     * thread
     * 
     * @param red
     * @param green
     * @param blue
     * @return Color for given colors
     * @throws IllegalArgumentException
     *             if the red, green or blue argument is not between 0 and 255
     */
    public static Color getColor(final int red, final int green, final int blue) {
        return getColor(Display.getCurrent(), red, green, blue);
    }

    /**
     * Get color based on RGB
     * 
     * @param display
     * @param red
     * @param green
     * @param blue
     * @return Color for given colors
     * @throws IllegalArgumentException
     *             if the red, green or blue argument is not between 0 and 255
     */
    public static Color getColor(final Display display, final int red, final int green, final int blue) {
        return getColor(display, new RGB(red, green, blue));
    }

    /**
     * Get color based on RGB. Can be called only from GUI thread
     * 
     * @param rgb
     * @return color instance
     */
    public static Color getColor(final RGB rgb) {
        return getColor(Display.getCurrent(), rgb);
    }

    /**
     * Gets color based on RGB.
     * 
     * @param display
     * @param rgb
     * @return color instance
     */
    public static Color getColor(final Display display, final RGB rgb) {
        Color color = COLOR_TABLE.get(rgb);
        if (color == null) {
            color = new Color(display, rgb);
            COLOR_TABLE.put(rgb, color);
        }
        return color;
    }

    /**
     * Dispose color manager.
     */
    public static void disposeColors() {
        for (final Color color : COLOR_TABLE.values()) {
            color.dispose();
        }
        COLOR_TABLE.clear();
    }

    public static RGB blend(final RGB val1, final RGB val2) {
        return new RGB(blend(val1.red, val2.red), blend(val1.green, val2.green), blend(val1.blue, val2.blue));
    }

    private static int blend(final int a, final int b) {
        return (a + b) / 2;
    }
}
