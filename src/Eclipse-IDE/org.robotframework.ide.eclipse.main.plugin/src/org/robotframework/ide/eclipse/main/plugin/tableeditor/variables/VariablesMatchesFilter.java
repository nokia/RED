/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.variables;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.robotframework.ide.eclipse.main.plugin.model.RobotVariable;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.HeaderFilterMatchesCollection;

public class VariablesMatchesFilter extends ViewerFilter {

    private final HeaderFilterMatchesCollection matches;

    public VariablesMatchesFilter(final HeaderFilterMatchesCollection matches) {
        this.matches = matches;
    }

    @Override
    public boolean select(final Viewer viewer, final Object parentElement, final Object element) {
        if (element instanceof RobotVariable) {
            return variableMatches((RobotVariable) element);
        }
        return true;
    }

    private boolean variableMatches(final RobotVariable variable) {
        return matches.contains(variable.getPrefix() + variable.getName() + variable.getSuffix())
                || matches.contains(variable.getValue())
                || matches.contains(variable.getComment());
    }
}
