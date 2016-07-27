/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model.cmd.cases;

import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.ModelType;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCase;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;

public class SetCaseKeywordCallNameCommand extends EditorCommand {

    private final RobotKeywordCall keywordCall;
    private final String name;

    public SetCaseKeywordCallNameCommand(final RobotKeywordCall keywordCall, final String name) {
        this.keywordCall = keywordCall;
        this.name = name;
    }

    @Override
    public void execute() throws CommandExecutionException {
        if (keywordCall.getName().equals(name)) {
            return;
        }

        if (isExecutable(keywordCall)) {
            if (looksLikeSetting()) {
                changeToSetting(keywordCall, name);
            } else {
                changeName(keywordCall, name);
            }
        } else {
            if (looksLikeSetting() && !isDifferentSetting()) {
                changeName(keywordCall, name);
            } else if (looksLikeSetting() && isDifferentSetting()) {
                changeToSetting(keywordCall, name);
            } else {
                changeToCall(keywordCall, name);
            }
        }
    }

    private boolean isDifferentSetting() {
        final RobotTokenType tokenType = RobotTokenType.findTypeOfDeclarationForTestCaseSettingTable(name);
        return !keywordCall.getLinkedElement().getDeclaration().getTypes().contains(tokenType);
    }

    private boolean isExecutable(final RobotKeywordCall call) {
        return call.getLinkedElement().getModelType() == ModelType.TEST_CASE_EXECUTABLE_ROW;
    }

    private boolean looksLikeSetting() {
        return name.startsWith("[") && name.endsWith("]");
    }

    private void changeToSetting(final RobotKeywordCall call, final String settingName) {
        final RobotCase testCase = (RobotCase) call.getParent();

        final int index = call.getIndex();
        testCase.removeChild(call);

        testCase.createSetting(index, settingName, call.getArguments(), call.getComment());

        eventBroker.send(RobotModelEvents.ROBOT_KEYWORD_CALL_ADDED, testCase);
    }

    private void changeToCall(final RobotKeywordCall call, final String name) {
        final RobotCase testCase = (RobotCase) call.getParent();

        final int index = call.getIndex();
        testCase.removeChild(call);

        testCase.createKeywordCall(index, name, call.getArguments(), call.getComment());

        eventBroker.send(RobotModelEvents.ROBOT_KEYWORD_CALL_ADDED, testCase);
    }

    private void changeName(final RobotKeywordCall element, final String name) {
        final AModelElement<?> linkedElement = element.getLinkedElement();
        linkedElement.getDeclaration().setText(name);

        eventBroker.send(RobotModelEvents.ROBOT_KEYWORD_CALL_NAME_CHANGE, keywordCall);
    }
}
