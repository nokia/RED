package org.robotframework.ide.eclipse.main.plugin;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IWorkbenchPage;

public class RobotFolder implements RobotElement {

    private final RobotElement parent;

    private final IFolder folder;

    private final List<RobotElement> elements;

    public RobotFolder(final RobotElement parent, final IFolder folder) {
        this.parent = parent;
        this.folder = folder;
        this.elements = new ArrayList<>();
    }

    public RobotFolder createRobotSuite(final IFolder folder) {
        if (folder == null) {
            return null;
        }
        final RobotFolder robotFolder = new RobotFolder(this, folder);
        if (elements.contains(robotFolder)) {
            return (RobotFolder) elements.get(elements.indexOf(robotFolder));
        } else {
            elements.add(robotFolder);
            return robotFolder;
        }
    }

    public RobotSuiteFile createSuiteFile(final IFile file) {
        if (file == null) {
            return null;
        }
        final RobotSuiteFile robotFile = new RobotSuiteFile(this, file);
        if (elements.contains(robotFile)) {
            return (RobotSuiteFile) elements.get(elements.indexOf(robotFile));
        } else {
            elements.add(robotFile);
            return robotFile;
        }
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        } else if (obj.getClass() == getClass()) {
            final RobotFolder other = (RobotFolder) obj;
            return folder.equals(other.folder);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return folder.hashCode();
    }

    @Override
    public String getName() {
        return folder.getName();
    }

    @Override
    public ImageDescriptor getImage() {
        return null;
    }

    @Override
    public OpenStrategy getOpenRobotEditorStrategy(final IWorkbenchPage page) {
        return new OpenStrategy() {};
    }

    @Override
    public RobotElement getParent() {
        return parent;
    }

    @Override
    public List<RobotElement> getChildren() {
        return elements;
    }
}
