/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model;

import org.eclipse.jface.text.Position;

import com.google.common.base.Optional;

/**
 * @author Michal Anglart
 *
 */
public interface RobotFileInternalElement extends RobotElement {

    /**
     * Gets the suite file in which this element is contained or null if it is
     * not inside the suite file.
     * 
     * @return Model object representing containg file.
     */
    RobotSuiteFile getSuiteFile();

    /**
     * Gets position of the whole element inside the file
     * 
     * @return position
     */
    Position getPosition();

    /**
     * Gets position of the defining token (usually name of the element)
     * 
     * @return position
     */
    Position getDefinitionPosition();

    /**
     * Gets model element for given offset in file
     * 
     * @param offset
     * @return
     */
    Optional<? extends RobotElement> findElement(final int offset);

    /**
     * Gets the comment of the element
     * 
     * @return Comment of the element
     */
    String getComment();
}
