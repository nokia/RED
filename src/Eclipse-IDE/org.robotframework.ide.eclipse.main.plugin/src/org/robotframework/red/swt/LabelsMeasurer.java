/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.red.swt;

import org.eclipse.swt.graphics.GC;

/**
 * @author Michal Anglart
 *
 */
public class LabelsMeasurer {

    /**
     * Returns prefix of given {@code text} which should be rendered under the assumption that there
     * are maximum {@code limit} pixels available in width.
     * 
     * @param gc
     * @param text
     * @param from
     * @param to
     * @param limit
     * @return
     */
    public static String cutTextToRender(final GC gc, final String text, final int limit) {
        // use binary search since label sizes are growing with each character, hence
        // it has same monotonic property as sorted arrays
        int low = 0;
        int high = text.length();

        while (low <= high) {
            final int mid = (low + high) >>> 1;
            final int midVal = gc.textExtent(text.substring(0, mid)).x;

            if (midVal < limit) {
                low = mid + 1;
            } else if (midVal > limit) {
                high = mid - 1;
            } else {
                return text.substring(0, mid);
            }
        }
        return high >= 0 ? text.substring(0, high) : "";
    }
}
