/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see licence.txt file for details.
 */
package org.robotframework.red.graphics;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

public class ImagesManager {
    private static final Map<ImageDescriptor, Image> IMAGES_TABLE = new HashMap<ImageDescriptor, Image>(10);

    private ImagesManager() {
        // nothing to do
    }

    public static int size() {
        return IMAGES_TABLE.size();
    }

    public static Image getImage(final ImageDescriptor imageDescriptor) {
        return getImage(Display.getCurrent(), imageDescriptor);
    }

    public static Image getImage(final Display display, final ImageDescriptor imageDescriptor) {
        if (imageDescriptor == null) {
            return null;
        }
        Image image = IMAGES_TABLE.get(imageDescriptor);
        if (image == null) {
            image = imageDescriptor.createImage(display);
            IMAGES_TABLE.put(imageDescriptor, image);
        }
        return image;
    }

    /**
     * Dispose images manager.
     */
    public static void disposeImages() {
        for (final Image image : IMAGES_TABLE.values()) {
            image.dispose();
        }
        IMAGES_TABLE.clear();
    }
}
