/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.mockmodel;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IWorkbenchPage;
import org.robotframework.ide.eclipse.main.plugin.model.RobotElement;

public class NamedElement implements RobotElement {

    private final String name;

    private final RobotElement parent;

    public NamedElement(final String name) {
        this(null, name);
    }

    public NamedElement(final RobotElement parent, final String name) {
        this.parent = parent;
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public RobotElement getParent() {
        return parent;
    }

    @Override
    public List<? extends RobotElement> getChildren() {
        return newArrayList();
    }

    @Override
    public int getIndex() {
        return parent == null ? -1 : parent.getChildren().indexOf(this);
    }

    @Override
    public ImageDescriptor getImage() {
        return null;
    }

    @Override
    public OpenStrategy getOpenRobotEditorStrategy(final IWorkbenchPage page) {
        return null;
    }
}
