package org.robotframework.ide.eclipse.main.plugin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IWorkbenchPage;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

public class RobotSuiteFile implements RobotElement {

    private final RobotElement parent;

    private final IFile file;

    private List<RobotElement> sections = null;


    public RobotSuiteFile(final RobotElement parent, final IFile file) {
        this.parent = parent;
        this.file = file;
    }

    public RobotSuiteFileSection createRobotSection(final String name) {
        final RobotSuiteFileSection section = new RobotSuiteFileSection(this, name, file == null ? false
                : file.isReadOnly());
        if (getSections().contains(section)) {
            return (RobotSuiteFileSection) sections.get(sections.indexOf(section));
        } else {
            sections.add(section);
            return section;
        }
    }

    public List<RobotElement> getSections() {
        if (sections == null) {
            sections = new ArrayList<>();
            try {
                sections.addAll(createParser().parseRobotFileSections(this));
            } catch (final IOException e) {
                throw new RuntimeException("Unable to read sections");
            }
        }
        return sections;
    }

    protected FileSectionsParser createParser() {
        return new FileSectionsParser(file);
    }

    public void refreshOnFileChange() {
        sections = null;
        getSections();
    }

    public List<RobotElementChange> synchronizeChanges() {
        sections = null;
        getSections();
        return new ArrayList<>();
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        } else if (obj.getClass() == getClass()) {
            final RobotSuiteFile other = (RobotSuiteFile) obj;
            return Objects.equals(file, other.file);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return file.hashCode();
    }

    @Override
    public String getName() {
        return file.getName();
    }

    @Override
    public ImageDescriptor getImage() {
        return null;
    }

    @Override
    public OpenStrategy getOpenRobotEditorStrategy(final IWorkbenchPage page) {
        return new OpenStrategy();
    }

    @Override
    public RobotElement getParent() {
        return parent;
    }

    public IFile getFile() {
        return file;
    }

    @Override
    public List<RobotElement> getChildren() {
        return sections == null ? Lists.<RobotElement> newArrayList() : sections;
    }

    public boolean isEditable() {
        return !file.isReadOnly();
    }

    @Override
    public boolean contains(final RobotElement element) {
        for (final RobotElement section : sections) {
            if (section.equals(element) || element.contains(section)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public RobotSuiteFile getSuiteFile() {
        return this;
    }

    public void commitChanges(final IProgressMonitor monitor) throws CoreException {
        file.setContents(new FilesSectionsEmiter(this).emit(), true, true, monitor);
    }

    public Optional<RobotElement> findVariablesSection() {
        return Iterables.tryFind(getSections(), new Predicate<RobotElement>() {
            @Override
            public boolean apply(final RobotElement element) {
                return element.getName().equals("Variables");
            }
        });
    }
}
