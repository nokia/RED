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

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.model.IWorkbenchAdapter;

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

    Collection<ProjectTreeElement> getAll() {
        final List<ProjectTreeElement> elements = new ArrayList<>();
        elements.add(this);

        for (final ProjectTreeElement child : children) {
            elements.addAll(child.getAll());
        }
        return elements;
    }

    public boolean isExcluded() {
        return isExcluded;
    }

    public void setExcluded(final boolean excluded) {
        this.isExcluded = excluded;
    }

    public boolean isInternalFolder() {
        return resource instanceof IFolder;
    }

    public boolean containsOtherFolders() {
        for (final ProjectTreeElement child : children) {
            if (child.isInternalFolder()) {
                return true;
            }
        }
        return false;
    }

    public IPath getPath() {
        return resource.getProjectRelativePath();
    }

    public void addChild(final ProjectTreeElement projectTreeElement) {
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
        return resource.getName();
    }

    String getLabel() {
        return resource.getName();
    }
}
