/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.editor.handlers;

import java.util.List;

import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfig.SearchPath;
import org.robotframework.red.viewers.Selections;

/**
 * @author Michal Anglart
 *
 */
abstract class E4DeleteSearchPathHandler {

    public void deleteSearchPaths(final List<SearchPath> paths, final IStructuredSelection selectedToRemove,
            final IEventBroker eventBroker, final String topic) {
        final List<SearchPath> pathsToRemove = Selections.getElements(selectedToRemove, SearchPath.class);

        boolean removed = false;
        for (final SearchPath pathToRemove : pathsToRemove) {
            removed |= paths.remove(pathToRemove);
        }
        if (removed) {
            eventBroker.send(topic, paths);
        }
    }
}
