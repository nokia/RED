/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.variables.nattable;

import org.robotframework.ide.eclipse.main.plugin.model.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotVariable;
import org.robotframework.ide.eclipse.main.plugin.model.RobotVariablesSection;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.HeaderFilterMatchesCollection;

class VariablesMatchesCollection extends HeaderFilterMatchesCollection {

    @Override
    public void collect(final RobotElement element, final String filter) {
        if (element instanceof RobotVariablesSection) {
            collectMatches((RobotVariablesSection) element, filter);
        }
    }

    private void collectMatches(final RobotVariablesSection section, final String filter) {
        for (final RobotVariable variable : section.getChildren()) {
            collectMatches(variable, filter);
        }
    }

    private void collectMatches(final RobotVariable variable, final String filter) {
        // or has to be greedy, since we want to find all matches
        final boolean isMatching = 
                collectMatches(filter, constructNameToSearch(variable))
                | collectMatches(filter, constructValueToSearch(variable)) 
                | collectMatches(filter, constructCommentToSearch(variable));
        if (isMatching) {
            rowsMatching++;
        }
    }

    private static String constructNameToSearch(final RobotVariable variable) {
        return variable.getPrefix() + variable.getName() + variable.getSuffix();
    }

    private static String constructValueToSearch(final RobotVariable variable) {
        return variable.getValue();
    }

    private static String constructCommentToSearch(final RobotVariable variable) {
        return variable.getComment();
    }

    static class VariableFilter {

        private final HeaderFilterMatchesCollection matches;

        public VariableFilter(final HeaderFilterMatchesCollection matches) {
            this.matches = matches;
        }

        boolean isMatching(final RobotVariable variable) {
            return matches.contains(constructNameToSearch(variable))
                    || matches.contains(constructValueToSearch(variable))
                    || matches.contains(constructCommentToSearch(variable));
        }
    }
}
