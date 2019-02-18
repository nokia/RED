/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model;

import static com.google.common.collect.Lists.newArrayList;

import java.util.ArrayList;
import java.util.List;

import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.code.ExecutablesRowHolderCommentService;

public class RobotEmptyLine extends RobotKeywordCall {

    private static final long serialVersionUID = 1L;

    public RobotEmptyLine(final IRobotCodeHoldingElement robotCodeHoldingElement,
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
    public RobotKeywordCall insertEmptyCellAt(final int position) {
        final List<RobotToken> tokens = ExecutablesRowHolderCommentService.execRowView(this);
        if (tokens.size() == 1 && !tokens.get(0).getTypes().contains(RobotTokenType.START_HASH_COMMENT)) {
            return this;

        } else if (position == 0 && tokens.size() > 1
                && !tokens.get(0).getTypes().contains(RobotTokenType.START_HASH_COMMENT)) {
            final RobotCodeHoldingElement<?> parent = (RobotCodeHoldingElement<?>) getParent();
            final int index = getIndex();
            parent.removeChild(this);
            final List<String> args = newArrayList("\\");
            final RobotKeywordCall resultCall = parent.createKeywordCall(index, "", args, getComment());
            resultCall.resetStored();
            return resultCall;

        } else {
            return super.insertEmptyCellAt(position);
        }
    }
}
