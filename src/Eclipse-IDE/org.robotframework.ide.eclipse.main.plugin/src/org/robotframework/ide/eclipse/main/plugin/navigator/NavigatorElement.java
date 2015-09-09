/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.navigator;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.resource.ImageDescriptor;

public abstract class NavigatorElement {

    private final IFile file;
    private NavigatorElement parent;
    private final List<NavigatorElement> children;

    public NavigatorElement(final IFile file, final NavigatorElement parent) {
        this.file = file;
        this.parent = parent;
        this.children = new ArrayList<>();
    }

    public IFile getFile() {
        return file;
    }

    public NavigatorElement getParent() {
        return parent;
    }

    private void setParent(final NavigatorElement parent) {
        this.parent = parent;
    }

    public List<NavigatorElement> getChildren() {
        return children;
    }

    public void addChild(final NavigatorElement child) {
        child.setParent(this);
        children.add(child);
    }

    public abstract String getLabel();

    public abstract String getName();

    public abstract ImageDescriptor getImage();

}
