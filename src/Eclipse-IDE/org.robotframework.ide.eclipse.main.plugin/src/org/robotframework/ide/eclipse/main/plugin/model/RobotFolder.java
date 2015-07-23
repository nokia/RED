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
