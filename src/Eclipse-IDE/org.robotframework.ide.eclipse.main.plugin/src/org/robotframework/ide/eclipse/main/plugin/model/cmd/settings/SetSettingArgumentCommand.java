/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model.cmd.settings;

import java.util.List;

import org.rf.ide.core.testdata.model.AKeywordBaseSetting;
import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.presenter.update.SettingTableModelUpdater;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.SetKeywordCallArgumentCommand;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;

public class SetSettingArgumentCommand extends SetKeywordCallArgumentCommand {
    
    public SetSettingArgumentCommand(final RobotKeywordCall keywordCall, final int index, final String value) {
        super(keywordCall, index, value);
    }

    public SetSettingArgumentCommand(final RobotKeywordCall keywordCall, final int index, final String value, final boolean shouldReplaceValue) {
        super(keywordCall, index, value, shouldReplaceValue);
    }

    @Override
    protected void updateModelElement(final List<String> arguments) {
        final AModelElement<?> linkedElement = keywordCall.getLinkedElement();
        final SettingTableModelUpdater updater = new SettingTableModelUpdater();
        if (value != null) {
            for (int i = arguments.size() - 1; i >= 0; i--) {
                updater.update(linkedElement, i, arguments.get(i));
            }
        } else {
            updater.update(linkedElement, index, value);
        }
    }

    @Override
    public List<EditorCommand> getUndoCommands() {
        return newUndoCommands(new SetSettingArgumentCommand(keywordCall, index, previousValue,
                index == 0 && keywordCall.getLinkedElement() instanceof AKeywordBaseSetting<?> ? true
                        : shouldReplaceValue));
    }
}
