/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.code.handler;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;

import org.robotframework.ide.eclipse.main.plugin.model.RobotCase;
import org.robotframework.ide.eclipse.main.plugin.model.RobotDefinitionSetting;
import org.robotframework.ide.eclipse.main.plugin.model.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordDefinition;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.InsertKeywordCallArgumentCommand;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.InsertKeywordDefinitionArgumentCommand;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.RemoveKeywordCallArgumentCommand;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.RemoveKeywordDefinitionArgumentCommand;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.SetKeywordCallArgumentCommand;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.SetKeywordCallCommentCommand;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.SetKeywordCallNameCommand;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.SetKeywordDefinitionArgumentCommand;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.SetKeywordDefinitionCommentCommand;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.SetKeywordDefinitionNameCommand;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.cases.SetCaseCommentCommand;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.cases.SetCaseNameCommand;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;

import com.google.common.base.Optional;

public class CodeAttributesCommandsProvider {

    public Optional<? extends EditorCommand> provideChangeAttributeCommand(final RobotElement element, final int index,
            final int noOfColumns, final String attribute) {
        if (element instanceof RobotKeywordDefinition) {
            final RobotKeywordDefinition definition = (RobotKeywordDefinition) element;
            if (index == 0) {
                return Optional.of(new SetKeywordDefinitionNameCommand(definition, attribute));
            } else if (index == noOfColumns - 1) {
                return Optional.of(new SetKeywordDefinitionCommentCommand(definition, attribute));
            } else {
                return Optional.of(new SetKeywordDefinitionArgumentCommand(definition, index - 1, attribute));
            }
        } else if (element instanceof RobotCase) {
            final RobotCase testCase = (RobotCase) element;
            if (index == 0) {
                return Optional.of(new SetCaseNameCommand(testCase, attribute));
            } else if (index == noOfColumns - 1) {
                return Optional.of(new SetCaseCommentCommand(testCase, attribute));
            }
        } else if (element instanceof RobotKeywordCall) {
            final RobotKeywordCall call = (RobotKeywordCall) element;
            if (index == 0) {
                return Optional.of(new SetKeywordCallNameCommand(call, attribute));
            } else if (index == noOfColumns - 1) {
                return Optional.of(new SetKeywordCallCommentCommand(call, attribute));
            } else {
                return Optional.of(new SetKeywordCallArgumentCommand(call, index - 1, attribute));
            }
        }
        return Optional.absent();
    }

    List<? extends EditorCommand> provideRemoveAttributeCommand(final RobotElement element, final int index) {
        if (element instanceof RobotKeywordDefinition) {
            final RobotKeywordDefinition definition = (RobotKeywordDefinition) element;
            if (index == 0) {
                final RobotDefinitionSetting argumentsSetting = definition.getArgumentsSetting();
                String name;
                if (argumentsSetting == null || argumentsSetting.getArguments().isEmpty()) {
                    name = "";
                } else {
                    name = argumentsSetting.getArguments().get(0);
                }

                final EditorCommand changeName = new SetKeywordDefinitionNameCommand(definition, name);
                final EditorCommand removeArgument = new RemoveKeywordDefinitionArgumentCommand(definition, 0);
                return newArrayList(changeName, removeArgument);
            } else {
                return newArrayList(new RemoveKeywordDefinitionArgumentCommand(definition, index - 1));
            }
        } else if (element instanceof RobotCase) {
            final RobotCase robotCase = (RobotCase) element;
            if (index == 0) {
                return newArrayList(new SetCaseNameCommand(robotCase, ""));
            }
        } else if (element instanceof RobotKeywordCall) {
            final RobotKeywordCall keywordCall = (RobotKeywordCall) element;
            if (index == 0) {
                final String name = keywordCall.getArguments().isEmpty() ? "" : keywordCall.getArguments().get(0);

                final EditorCommand changeName = new SetKeywordCallNameCommand(keywordCall, name);
                final EditorCommand removeArgument = new RemoveKeywordCallArgumentCommand(keywordCall, 0);
                return newArrayList(changeName, removeArgument);
            } else {
                return newArrayList(new RemoveKeywordCallArgumentCommand(keywordCall, index - 1));
            }
        }
        return newArrayList();
    }

    List<? extends EditorCommand> provideInsertAttributeCommands(final RobotElement element, final int index) {

        if (element instanceof RobotKeywordDefinition) {
            final RobotKeywordDefinition definition = (RobotKeywordDefinition) element;
            if (index == 0) {
                final String currentName = definition.getName();

                final EditorCommand changeName = new SetKeywordDefinitionNameCommand(definition, "");
                final EditorCommand insertArgument = new InsertKeywordDefinitionArgumentCommand(definition, 0,
                        currentName);
                return newArrayList(changeName, insertArgument);
            } else {
                return newArrayList(new InsertKeywordDefinitionArgumentCommand(definition, index - 1, ""));
            }
        } else if (element instanceof RobotCase) {
            final RobotCase robotCase = (RobotCase) element;
            if (index == 0) {
                return newArrayList(new SetCaseNameCommand(robotCase, ""));
            }
        } else if (element instanceof RobotKeywordCall) {
            final RobotKeywordCall keywordCall = (RobotKeywordCall) element;
            if (index == 0) {
                final String currentName = keywordCall.getName();

                final EditorCommand changeName = new SetKeywordCallNameCommand(keywordCall, "");
                final EditorCommand insertArgument = new InsertKeywordCallArgumentCommand(keywordCall, 0, currentName);
                return newArrayList(changeName, insertArgument);
            } else {
                return newArrayList(new InsertKeywordCallArgumentCommand(keywordCall, index - 1, ""));
            }
        }
        return newArrayList();
    }
}
