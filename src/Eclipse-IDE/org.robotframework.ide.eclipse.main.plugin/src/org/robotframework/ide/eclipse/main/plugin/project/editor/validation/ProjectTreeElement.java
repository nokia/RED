/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.editor.validation;

import static com.google.common.collect.Lists.newArrayList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.robotframework.ide.eclipse.main.plugin.RedImages;

import com.google.common.base.Objects;

/**
 * @author Michal Anglart
 *
 */
public class ProjectTreeElement implements IWorkbenchAdapter {

    private final IResource resource;

    private boolean isExcluded;

    private ProjectTreeElement parent;

    private final List<ProjectTreeElement> children = newArrayList();

    public ProjectTreeElement(final IResource resource, final boolean isExcluded) {
        this.resource = resource;
        this.isExcluded = isExcluded;
    }

    public boolean isVirtual() {
        return false;
    }

    public IPath getPath() {
        return resource.getProjectRelativePath();
    }

    public String getName() {
        return resource.getName();
    }

    public boolean isExcluded() {
        return isExcluded;
    }

    public void setExcluded(final boolean excluded) {
        this.isExcluded = excluded;
    }

    public boolean isExcludedViaInheritance() {
        ProjectTreeElement current = this;
        while (current.parent != null) {
            if (current.isExcluded) {
                return true;
            }
            current = current.parent;
        }
        return false;
    }

    public boolean isInternalFolder() {
        return resource instanceof IFolder;
    }
    public boolean isFile() {
        return resource instanceof IFile;
    }

    Collection<ProjectTreeElement> getAll() {
        final List<ProjectTreeElement> elements = new ArrayList<>();
        elements.add(this);

        for (final ProjectTreeElement child : children) {
            elements.addAll(child.getAll());
        }
        return elements;
    }

    public boolean containsOtherFolders() {
        for (final ProjectTreeElement child : children) {
            if (child.isInternalFolder()) {
                return true;
            }
        }
        return false;
    }

    public void createVirtualNodeFor(final IPath path) {
        createVirtualNodeFor(new Path(""), path);
    }

    protected void createVirtualNodeFor(final IPath currentPath, final IPath path) {
        if (resource instanceof IWorkspaceRoot) {
            children.get(0).createVirtualNodeFor(currentPath, path);
            return;
        }
        if (path.isEmpty()) {
            isExcluded = true;
            return;
        }

        final String firstSegment = path.segment(0);
        for (final ProjectTreeElement child : children) {
            if (child.getName().equals(firstSegment)) {
                child.createVirtualNodeFor(currentPath.append(firstSegment), path.removeFirstSegments(1));
                return;
            }
        }
        final VirtualProjectTreeElement newChild = new VirtualProjectTreeElement(currentPath.append(firstSegment),
                path.segmentCount() == 1);
        addChild(newChild);
        newChild.createVirtualNodeFor(currentPath.append(firstSegment), path.removeFirstSegments(1));
    }

    public void addChild(final ProjectTreeElement projectTreeElement) {
        projectTreeElement.parent = this;
        children.add(projectTreeElement);
    }

    @Override
    public Object getParent(final Object o) {
        return parent;
    }

    @Override
    public Object[] getChildren(final Object o) {
        return children.toArray();
    }

    @Override
    public ImageDescriptor getImageDescriptor(final Object object) {
        return resource.getAdapter(IWorkbenchAdapter.class).getImageDescriptor(resource);
    }

    ImageDescriptor getImageDescriptor() {
        return getImageDescriptor(null);
    }

    @Override
    public String getLabel(final Object o) {
        return getLabel();
    }

    String getLabel() {
        return resource.getName();
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj != null && obj.getClass() == ProjectTreeElement.class) {
            final ProjectTreeElement that = (ProjectTreeElement) obj;
            return Objects.equal(this.resource, that.resource);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(resource);
    }

    static class VirtualProjectTreeElement extends ProjectTreeElement {

        private final IPath path;

        public VirtualProjectTreeElement(final IPath path, final boolean isExcluded) {
            super(null, isExcluded);
            this.path = path;
        }

        @Override
        public boolean isVirtual() {
            return true;
        }

        @Override
        public IPath getPath() {
            return path;
        }

        @Override
        public String getName() {
            return path.lastSegment();
        }

        @Override
        public boolean isInternalFolder() {
            return true;
        }

        @Override
        public ImageDescriptor getImageDescriptor(final Object object) {
            return RedImages.getGrayedImage(RedImages.getFolderImage());
        }

        @Override
        public String getLabel(final Object o) {
            return getLabel();
        }

        @Override
        String getLabel() {
            return getName();
        }

        @Override
        public boolean equals(final Object obj) {
            if (obj != null && obj.getClass() == VirtualProjectTreeElement.class) {
                final VirtualProjectTreeElement that = (VirtualProjectTreeElement) obj;
                return Objects.equal(this.path, that.path);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(path);
        }
    }

    public boolean isProject() {
        return resource instanceof IProject;

    }

}
