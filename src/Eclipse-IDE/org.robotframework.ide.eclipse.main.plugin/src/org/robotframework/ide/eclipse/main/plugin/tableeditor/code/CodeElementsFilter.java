/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.code;

import org.robotframework.ide.eclipse.main.plugin.model.RobotCodeHoldingElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.HeaderFilterMatchesCollection;

public class CodeElementsFilter {

    private final HeaderFilterMatchesCollection matches;

    public CodeElementsFilter(final HeaderFilterMatchesCollection matches) {
        this.matches = matches;
    }

    public boolean isMatching(final Object rowObject) {
        if (rowObject instanceof RobotCodeHoldingElement<?>) {
            return isMatching((RobotCodeHoldingElement<?>) rowObject);
        } else if (rowObject instanceof RobotKeywordCall) {
            return isMatching((RobotKeywordCall) rowObject);
        }
        return false;
    }

    private boolean isMatching(final RobotCodeHoldingElement<?> holder) {
        return matches.contains(holder.getName()) || matches.contains(holder.getComment())
                || holder.getChildren().stream().anyMatch(this::isMatching);
    }

    private boolean isMatching(final RobotKeywordCall call) {
        return matches.contains(call.getLabel()) || matches.contains(call.getComment())
                || call.getArguments().stream().anyMatch(matches::contains);
    }
}