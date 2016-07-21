/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model.cmd;

import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.presenter.update.KeywordTableModelUpdater;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;

public class SetKeywordSettingCommentCommand extends SetKeywordCallCommentCommand {

    public SetKeywordSettingCommentCommand(final RobotKeywordCall keywordCall, final String comment) {
        super(keywordCall, comment);
    }

    @Override
    protected void updateModelElement() {
        final AModelElement<?> linkedElement = getKeywordCall().getLinkedElement();
        new KeywordTableModelUpdater().updateComment(linkedElement, getNewComment());
    }
}
