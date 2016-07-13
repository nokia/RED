/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.cases.nattable;

import org.robotframework.ide.eclipse.main.plugin.model.RobotCase;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCasesSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.HeaderFilterMatchesCollection;

public class CasesMatchesCollection extends HeaderFilterMatchesCollection {

    @Override
    public void collect(final RobotElement element, final String filter) {
        if (element instanceof RobotCasesSection) {
            collectMatches((RobotCasesSection) element, filter);
        }
    }

    private void collectMatches(final RobotCasesSection section, final String filter) {
        for (final RobotCase testCase : section.getChildren()) {
            collectMatches(testCase, filter);
        }
    }

    private void collectMatches(final RobotCase testCase, final String filter) {
        boolean isMatching = false;
        isMatching |= collectMatches(filter, testCase.getName());
        isMatching |= collectMatches(filter, testCase.getComment());
        if (isMatching) {
            rowsMatching++;
        }

        for (final RobotKeywordCall call : testCase.getChildren()) {
            collectMatches(call, filter);
        }
    }

    private void collectMatches(final RobotKeywordCall call, final String filter) {
        boolean isMatching = false;
        isMatching |= collectMatches(filter, call.getName());
        for (final String arg : call.getArguments()) {
            isMatching |= collectMatches(filter, arg);
        }
        isMatching |= collectMatches(filter, call.getComment());
        if (isMatching) {
            rowsMatching++;
        }
    }

    static class CasesFilter {

        private final HeaderFilterMatchesCollection matches;

        public CasesFilter(final HeaderFilterMatchesCollection matches) {
            this.matches = matches;
        }

        public boolean isMatching(final Object rowObject) {
            if (rowObject instanceof RobotCase) {
                return isMatching((RobotCase) rowObject);
            } else if (rowObject instanceof RobotKeywordCall) {
                return isMatching((RobotKeywordCall) rowObject);
            }
            return true;
        }

        boolean isMatching(final RobotCase testCase) {
            return matches.contains(testCase.getName()) || matches.contains(testCase.getComment())
                    || hasMatchingCall(testCase);
        }

        private boolean hasMatchingCall(final RobotCase testCase) {
            for (final RobotKeywordCall call : testCase.getChildren()) {
                if (isMatching(call)) {
                    return true;
                }
            }
            return false;
        }

        boolean isMatching(final RobotKeywordCall call) {
            return matches.contains(call.getName()) || matches.contains(call.getComment()) || hasMatchingArgument(call);
        }

        private boolean hasMatchingArgument(final RobotKeywordCall call) {
            for (final String arg : call.getArguments()) {
                if (matches.contains(arg)) {
                    return true;
                }
            }
            return false;
        }
    }
}
