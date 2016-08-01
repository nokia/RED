/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model.cmd.cases;

import java.util.List;

import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.ModelType;
import org.rf.ide.core.testdata.model.presenter.update.TestCaseTableModelUpdater;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;
import org.robotframework.ide.eclipse.main.plugin.views.DocumentationView;

public class SetCaseKeywordCallArgumentCommand extends EditorCommand {

    private final RobotKeywordCall keywordCall;
    private final int index;
    private final String value;

    public SetCaseKeywordCallArgumentCommand(final RobotKeywordCall keywordCall, final int index, final String value) {
        this.keywordCall = keywordCall;
        this.index = index;
        this.value = value;
    }

    @Override
    public void execute() throws CommandExecutionException {
        final List<String> arguments = keywordCall.getArguments();
        keywordCall.resetStored();

        boolean changed = false;
        for (int i = arguments.size(); i <= index; i++) {
            arguments.add("\\");
            changed = true;
        }
        changed |= index < arguments.size() && !arguments.get(index).equals(value);
        arguments.set(index, value.isEmpty() ? "\\" : value);

        for (int i = arguments.size() - 1; i >= 0; i--) {
            if (!arguments.get(i).equals("\\")) {
                break;
            }
            arguments.set(i, null);
        }

        if (changed) {
            final AModelElement<?> linkedElement = keywordCall.getLinkedElement();
            
            final TestCaseTableModelUpdater updater = new TestCaseTableModelUpdater();
            for (int i = arguments.size() - 1; i >= 0; i--) {
                updater.updateArgument(linkedElement, i, arguments.get(i));
            }
            
            if (linkedElement.getModelType() == ModelType.TEST_CASE_DOCUMENTATION) {
                eventBroker.post(DocumentationView.REFRESH_DOC_EVENT_TOPIC, keywordCall);
            }
            
            eventBroker.send(RobotModelEvents.ROBOT_KEYWORD_CALL_ARGUMENT_CHANGE, keywordCall);
        }
    }
}
