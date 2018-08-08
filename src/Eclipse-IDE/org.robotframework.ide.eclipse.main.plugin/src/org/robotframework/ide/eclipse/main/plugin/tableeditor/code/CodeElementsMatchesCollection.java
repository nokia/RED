/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.code;

import org.robotframework.ide.eclipse.main.plugin.model.RobotCodeHoldingElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotFileInternalElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFileSection;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.HeaderFilterMatchesCollection;

public class CodeElementsMatchesCollection extends HeaderFilterMatchesCollection {

    @Override
    public void collect(final RobotElement element, final String filter) {
        if (element instanceof RobotSuiteFileSection) {
            collectMatches((RobotSuiteFileSection) element, filter);
        }
    }

    private void collectMatches(final RobotSuiteFileSection section, final String filter) {
        for (final RobotFileInternalElement element : section.getChildren()) {
            collectMatches((RobotCodeHoldingElement<?>) element, filter);
        }
    }

    private void collectMatches(final RobotCodeHoldingElement<?> holder, final String filter) {
        boolean isMatching = false;
        isMatching |= collectMatches(filter, holder.getName());
        isMatching |= collectMatches(filter, holder.getComment());
        if (isMatching) {
            rowsMatching++;
        }

        for (final RobotKeywordCall call : holder.getChildren()) {
            collectMatches(call, filter);
        }
    }

    private void collectMatches(final RobotKeywordCall call, final String filter) {
        boolean isMatching = false;
        if (shouldMatchLabel(call)) {
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

    protected boolean shouldMatchLabel(@SuppressWarnings("unused") final RobotKeywordCall call) {
        return true;
    }
}
