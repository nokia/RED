/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model.cmd;

import static com.google.common.collect.Lists.newArrayList;

import java.util.ArrayList;
import java.util.List;

import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.presenter.update.IExecutablesTableModelUpdater;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCodeHoldingElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotDefinitionSetting;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;
import org.robotframework.ide.eclipse.main.plugin.views.DocumentationView;

public class SetKeywordCallArgumentCommand extends EditorCommand {

    protected final RobotKeywordCall keywordCall;
    protected final int index;
    protected final String value;

    public SetKeywordCallArgumentCommand(final RobotKeywordCall keywordCall, final int index, final String value) {
        this.keywordCall = keywordCall;
        this.index = index;
        this.value = value;
    }

    @Override
    public void execute() throws CommandExecutionException {
        final List<String> arguments = prepareArgumentsList(keywordCall, value, index);

        if (!arguments.equals(keywordCall.getArguments())) {
            updateModelElement(arguments);
            keywordCall.resetStored();

            if (keywordCall instanceof RobotDefinitionSetting
                    && ((RobotDefinitionSetting) keywordCall).isDocumentation()) {
                eventBroker.post(DocumentationView.REFRESH_DOC_EVENT_TOPIC, keywordCall);
            }
            eventBroker.send(RobotModelEvents.ROBOT_KEYWORD_CALL_ARGUMENT_CHANGE, keywordCall);
        }
    }

    public static List<String> prepareArgumentsList(final RobotKeywordCall call, final String value, final int index) {
        final List<String> arguments = call == null ? new ArrayList<String>() : newArrayList(call.getArguments());

        for (int i = arguments.size(); i <= index; i++) {
            arguments.add("\\");
        }
        arguments.set(index, value == null || value.trim().isEmpty() ? "\\" : value);
        for (int i = arguments.size() - 1; i >= 0; i--) {
            if (!arguments.get(i).equals("\\")) {
                break;
            }
            arguments.set(i, null);
        }
        return arguments;
    }

    protected void updateModelElement(final List<String> arguments) {
        final RobotCodeHoldingElement<?> parent = (RobotCodeHoldingElement<?>) keywordCall.getParent();
        final IExecutablesTableModelUpdater<?> updater = parent.getModelUpdater();

        final AModelElement<?> linkedElement = keywordCall.getLinkedElement();
        if (value != null) {
            for (int i = arguments.size() - 1; i >= 0; i--) {
                updater.updateArgument(linkedElement, i, arguments.get(i));
            }
        } else {
            updater.updateArgument(linkedElement, index, value);
        }
    }
}
