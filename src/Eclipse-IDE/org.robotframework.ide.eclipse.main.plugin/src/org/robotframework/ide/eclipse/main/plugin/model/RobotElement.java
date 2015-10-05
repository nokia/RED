/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model;

import java.util.List;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IWorkbenchPage;

/**
 * An interface which all Robot model objects has to implement
 * 
 * @{author Michal Anglart
 */
public interface RobotElement {

    /**
     * Gets the name of the element
     * 
     * @return Element name
     */
    String getName();

    /**
     * Gets parent of this element
     * 
     * @return
     */
    RobotElement getParent();

    /**
     * Gets children elements
     * 
     * @return List of children elements
     */
    List<? extends RobotElement> getChildren();

    /**
     * Gets image descriptor of this element
     * 
     * @return image descriptor
     */
    ImageDescriptor getImage();

    /**
     * Returns open strategy capable of opening and selecting this element in
     * editor.
     * 
     * @param page
     * @return
     */
    OpenStrategy getOpenRobotEditorStrategy(IWorkbenchPage page);

    /**
     * The strategy for opening given this element in editor.
     */
    public class OpenStrategy {
        public void run() {

        }
    }
}
