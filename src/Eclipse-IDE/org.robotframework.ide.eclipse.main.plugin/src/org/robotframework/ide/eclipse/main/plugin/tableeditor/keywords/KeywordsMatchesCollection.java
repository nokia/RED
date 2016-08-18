/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.keywords;

import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;
import org.robotframework.ide.eclipse.main.plugin.model.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordDefinition;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordsSection;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.HeaderFilterMatchesCollection;

public class KeywordsMatchesCollection extends HeaderFilterMatchesCollection {

    @Override
    public void collect(final RobotElement element, final String filter) {
        if (element instanceof RobotKeywordsSection) {
            collectMatches((RobotKeywordsSection) element, filter);
        }
    }

    public void collectMatches(final RobotKeywordsSection section, final String filter) {
        for (final RobotKeywordDefinition keywordDefinition : section.getChildren()) {
            collectMatches(keywordDefinition, filter);
        }
    }

    private void collectMatches(final RobotKeywordDefinition keywordDefinition, final String filter) {
        boolean isMatching = false;
        isMatching |= collectMatches(filter, keywordDefinition.getName());
        isMatching |= collectMatches(filter, keywordDefinition.getComment());
        if (isMatching) {
            rowsMatching++;
        }

        for (final RobotKeywordCall call : keywordDefinition.getChildren()) {
            collectMatches(call, filter);
        }
    }

    private void collectMatches(final RobotKeywordCall call, final String filter) {
        boolean isMatching = false;
        if (!call.getLinkedElement().getDeclaration().getTypes().contains(RobotTokenType.KEYWORD_SETTING_ARGUMENTS)) {
            isMatching |= collectMatches(filter, call.getLabel());
        }
        for (final String arg : call.getArguments()) {
            isMatching |= collectMatches(filter, arg);
        }
        isMatching |= collectMatches(filter, call.getComment());
        if (isMatching) {
            rowsMatching++;
        }
    }

    static class KeywordsFilter {

        private final HeaderFilterMatchesCollection matches;

        public KeywordsFilter(final HeaderFilterMatchesCollection matches) {
            this.matches = matches;
        }

        public boolean isMatching(final Object rowObject) {
            if (rowObject instanceof RobotKeywordDefinition) {
                return isMatching((RobotKeywordDefinition) rowObject);
            } else if (rowObject instanceof RobotKeywordCall) {
                return isMatching((RobotKeywordCall) rowObject);
            }
            return false;
        }

        boolean isMatching(final RobotKeywordDefinition keywordDefinition) {
            return matches.contains(keywordDefinition.getName()) || matches.contains(keywordDefinition.getComment())
                    || hasMatchingCall(keywordDefinition);
        }

        private boolean hasMatchingCall(final RobotKeywordDefinition keywordDefinition) {
            for (final RobotKeywordCall call : keywordDefinition.getChildren()) {
                if (isMatching(call)) {
                    return true;
                }
            }
            return false;
        }

        boolean isMatching(final RobotKeywordCall call) {
            return matches.contains(call.getLabel()) || matches.contains(call.getComment())
                    || hasMatchingArgument(call);
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
