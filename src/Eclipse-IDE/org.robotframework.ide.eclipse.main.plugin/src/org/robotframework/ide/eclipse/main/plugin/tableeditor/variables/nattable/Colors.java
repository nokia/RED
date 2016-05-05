/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.variables.nattable;

import org.eclipse.swt.graphics.RGB;

/**
 * @author Michal Anglart
 *
 */
public class Colors {

    private static final int LIMIT = 130;

    boolean isDarkColor(final RGB color) {
        return calculatePerceivedBrightness(color) > LIMIT;
    }

    boolean isBrightColor(final RGB color) {
        return !isDarkColor(color);
    }

    // the formula is referenced in the internet in topics regarding perceived brightness
    private int calculatePerceivedBrightness(final RGB color) {
        final int r = color.red;
        final int g = color.green;
        final int b = color.blue;
        return (int) Math.sqrt(r * r * .241 + g * g * .691 + b * b * .068);
    }
}
