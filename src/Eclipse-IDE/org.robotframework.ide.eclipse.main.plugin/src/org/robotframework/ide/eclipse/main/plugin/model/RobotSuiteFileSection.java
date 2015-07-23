package org.robotframework.ide.eclipse.main.plugin.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IWorkbenchPage;
import org.robotframework.ide.eclipse.main.plugin.RobotImages;

public class RobotSuiteFileSection implements RobotElement {

    private final IFile file;
    private final String name;
    private final boolean readOnly;

    private final RobotElement parent;
    protected final List<RobotElement> elements = new ArrayList<>();

    RobotSuiteFileSection(final RobotSuiteFile parent, final String name, final boolean readOnly) {
        this.parent = parent;
        this.file = parent == null ? null : parent.getFile();
        this.name = name;
        this.readOnly = readOnly;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        } else if (obj.getClass() == getClass()) {
            final RobotSuiteFileSection other = (RobotSuiteFileSection) obj;
            return Objects.equals(file, other.file) && name.equals(other.name);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(file, name);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getComment() {
        return "";
    }

    @Override
    public ImageDescriptor getImage() {
        return RobotImages.getRobotCasesFileSectionImage();
    }

    @Override
    public OpenStrategy getOpenRobotEditorStrategy(final IWorkbenchPage page) {
        return new PageActivatingOpeningStrategy(page, file, RobotSuiteFileSection.this);
    }

    public IFile getFile() {
        return file;
    }

    @Override
    public RobotElement getParent() {
        return parent;
    }

    @Override
    public List<? extends RobotElement> getChildren() {
        return elements;
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    @Override
    public RobotSuiteFile getSuiteFile() {
        return (RobotSuiteFile) this.getParent();
    }
}
