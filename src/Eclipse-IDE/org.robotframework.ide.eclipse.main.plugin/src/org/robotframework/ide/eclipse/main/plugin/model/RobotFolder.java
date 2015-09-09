/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model;

import org.eclipse.core.resources.IFolder;

public class RobotFolder extends RobotContainer {

    RobotFolder(final RobotElement parent, final IFolder folder) {
        super(parent, folder);
    }

    public IFolder getFolder() {
        return (IFolder) container;
    }
}
