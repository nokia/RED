/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model;

import java.util.ArrayList;
import java.util.List;

import org.rf.ide.core.testdata.model.AModelElement;

public class RobotEmptyLine extends RobotKeywordCall {

    private static final long serialVersionUID = 1L;

    public RobotEmptyLine(final RobotCodeHoldingElement<?> robotCodeHoldingElement,
            final AModelElement<?> linkedElement) {
        super(robotCodeHoldingElement, linkedElement);
    }

    @Override
    public List<String> getArguments() {
        return new ArrayList<>();
    }

    @Override
    public boolean shouldAddCommentMark() {
        return false;
    }

    @Override
    public RobotKeywordCall insertCellAt(int position, String newValue) {
        // nothing to do
        return this;
    }
}
