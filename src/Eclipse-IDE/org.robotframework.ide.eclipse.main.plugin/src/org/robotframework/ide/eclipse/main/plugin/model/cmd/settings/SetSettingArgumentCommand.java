/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model.cmd.settings;

import java.util.List;

import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.presenter.update.SettingTableModelUpdater;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.SetKeywordCallArgumentCommand;

public class SetSettingArgumentCommand extends SetKeywordCallArgumentCommand {

    public SetSettingArgumentCommand(final RobotKeywordCall keywordCall, final int index,
            final String value) {
        super(keywordCall, index, value);
    }

    @Override
    protected void updateModelElement(final List<String> arguments) {
        final AModelElement<?> linkedElement = keywordCall.getLinkedElement();
        new SettingTableModelUpdater().update(linkedElement, index, value);
    }

}
