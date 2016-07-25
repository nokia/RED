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

public class ParentElement implements RobotElement {

    private final List<RobotElement> children = newArrayList();

    public ParentElement(final RobotElement... children) {
        for (final RobotElement child : children) {
            this.children.add(child);
        }
    }

    @Override
    public String getName() {
        return "";
    }

    @Override
    public RobotElement getParent() {
        return null;
    }

    @Override
    public List<RobotElement> getChildren() {
        return children;
    }

    @Override
    public int getIndex() {
        return -1;
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
