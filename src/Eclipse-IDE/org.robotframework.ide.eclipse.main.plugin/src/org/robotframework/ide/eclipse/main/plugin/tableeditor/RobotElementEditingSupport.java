/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor;

import org.eclipse.jface.viewers.ColumnViewer;
import org.robotframework.ide.eclipse.main.plugin.model.RobotElement;
import org.robotframework.red.viewers.ElementsAddingEditingSupport;

public abstract class RobotElementEditingSupport extends ElementsAddingEditingSupport {

    // Id of context which should be activated when cell editor is activated
    public static final String DETAILS_EDITING_CONTEXT_ID = "org.robotframework.ide.eclipse.details.context";

    protected final RobotEditorCommandsStack commandsStack;

    public RobotElementEditingSupport(final ColumnViewer viewer, final int index,
            final RobotEditorCommandsStack commandsStack, final NewElementsCreator<RobotElement> creator) {
        super(viewer, index, creator);
        this.commandsStack = commandsStack;
    }
}