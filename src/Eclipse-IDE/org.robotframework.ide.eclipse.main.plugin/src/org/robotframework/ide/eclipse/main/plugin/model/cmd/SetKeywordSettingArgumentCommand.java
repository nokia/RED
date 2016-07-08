/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model.cmd;

import org.rf.ide.core.testdata.model.presenter.update.KeywordTableModelUpdater;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;

public class SetKeywordSettingArgumentCommand extends SetKeywordCallArgumentCommand {

    public SetKeywordSettingArgumentCommand(final RobotKeywordCall keywordCall, final int index, final String value) {
        super(keywordCall, index, value);
    }

    protected void updateModelElement() {
        new KeywordTableModelUpdater().update(getKeywordCall().getLinkedElement(), getIndex(), getValue());
    }

}
