/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.code;

import java.util.List;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCase;
import org.robotframework.ide.eclipse.main.plugin.model.RobotDefinitionSetting;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordDefinition;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.HeaderFilterMatchesCollection;

public class CodeMatchesFilter extends ViewerFilter {

    private final HeaderFilterMatchesCollection matches;

    public CodeMatchesFilter(final HeaderFilterMatchesCollection matches) {
        this.matches = matches;
    }

    @Override
    public boolean select(final Viewer viewer, final Object parentElement, final Object element) {
        if (element instanceof RobotCase) {
            final RobotCase testCase = (RobotCase) element;
            return caseMatches(testCase) || keywordCallsMatches(testCase.getChildren());
        } else if (element instanceof RobotKeywordDefinition) {
            final RobotKeywordDefinition keywordDef = (RobotKeywordDefinition) element;
            return keywordMatches(keywordDef) || keywordCallsMatches(keywordDef.getChildren());
        } else if (element instanceof RobotKeywordCall) {
            return keywordCallMatches((RobotKeywordCall) element);
        }
        return true;
    }

    private boolean caseMatches(final RobotCase element) {
        return matches.contains(element.getName()) || matches.contains(element.getComment());
    }

    private boolean keywordMatches(final RobotKeywordDefinition element) {
        return matches.contains(element.getName()) || matches.contains(element.getComment())
                || argumentsMatches(element.getArgumentsSetting());
    }

    private boolean keywordCallsMatches(final List<RobotKeywordCall> elements) {
        boolean matches = false;
        for (final RobotKeywordCall call : elements) {
            matches |= keywordCallMatches(call);
        }
        return matches;
    }

    private boolean keywordCallMatches(final RobotKeywordCall element) {
        return matches.contains(element.getName()) || matches.contains(element.getComment())
                || argumentsMatches(element.getArguments());
    }

    private boolean argumentsMatches(final RobotDefinitionSetting argumentsSetting) {
        if (argumentsSetting == null || argumentsSetting.getArguments() == null) {
            return false;
        }
        return argumentsMatches(argumentsSetting.getArguments());
    }

    private boolean argumentsMatches(final List<String> arguments) {
        for (final String argument : arguments) {
            if (matches.contains(argument)) {
                return true;
            }
        }
        return false;
    }
}
