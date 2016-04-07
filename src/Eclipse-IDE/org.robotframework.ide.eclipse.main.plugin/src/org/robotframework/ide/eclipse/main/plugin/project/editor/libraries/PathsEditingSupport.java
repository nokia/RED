/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.editor.libraries;

import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.widgets.Composite;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfig.SearchPath;
import org.robotframework.red.viewers.ElementsAddingEditingSupport;


/**
 * @author Michal Anglart
 *
 */
class PathsEditingSupport extends ElementsAddingEditingSupport {

    private final String topic;

    private final IEventBroker eventBroker;

    PathsEditingSupport(final ColumnViewer viewer, final NewElementsCreator<SearchPath> creator,
            final IEventBroker eventBroker, final String pathChangeEventTopic) {
        super(viewer, 0, creator);
        this.eventBroker = eventBroker;
        this.topic = pathChangeEventTopic;
    }

    @Override
    protected boolean canEdit(final Object element) {
        return !(element instanceof SearchPath && ((SearchPath) element).isSystem());
    }

    @Override
    protected CellEditor getCellEditor(final Object element) {
        if (element instanceof SearchPath) {
            return new TextCellEditor((Composite) getViewer().getControl());
        } else {
            return super.getCellEditor(element);
        }
    }

    @Override
    protected Object getValue(final Object element) {
        if (element instanceof SearchPath) {
            return ((SearchPath) element).getPath();
        } else {
            return null;
        }
    }

    @Override
    protected void setValue(final Object element, final Object value) {
        if (element instanceof SearchPath) {
            ((SearchPath) element).setPath((String) value);

            eventBroker.send(topic, element);
        } else {
            super.setValue(element, value);
        }
    }
}
