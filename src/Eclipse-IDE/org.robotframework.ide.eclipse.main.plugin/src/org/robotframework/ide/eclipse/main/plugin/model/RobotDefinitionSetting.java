/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model;

import static java.util.stream.Collectors.toList;

import java.util.EnumSet;
import java.util.List;

import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.ExecutableSetting;
import org.rf.ide.core.testdata.model.ModelType;
import org.rf.ide.core.testdata.model.table.LocalSetting;
import org.rf.ide.core.testdata.text.read.IRobotTokenType;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

public class RobotDefinitionSetting extends RobotKeywordCall {

    private static final EnumSet<RobotTokenType> NON_ARG_TYPES = EnumSet.of(RobotTokenType.START_HASH_COMMENT,
            RobotTokenType.COMMENT_CONTINUE, RobotTokenType.TEST_CASE_SETTING_SETUP,
            RobotTokenType.TEST_CASE_SETTING_DOCUMENTATION, RobotTokenType.TEST_CASE_SETTING_TAGS_DECLARATION,
            RobotTokenType.TEST_CASE_SETTING_TEARDOWN, RobotTokenType.TEST_CASE_SETTING_TEMPLATE,
            RobotTokenType.TEST_CASE_SETTING_TIMEOUT, RobotTokenType.TEST_CASE_SETTING_UNKNOWN_DECLARATION,
            RobotTokenType.TASK_SETTING_SETUP, RobotTokenType.TASK_SETTING_DOCUMENTATION,
            RobotTokenType.TASK_SETTING_TAGS_DECLARATION, RobotTokenType.TASK_SETTING_TEARDOWN,
            RobotTokenType.TASK_SETTING_TEMPLATE, RobotTokenType.TASK_SETTING_TIMEOUT,
            RobotTokenType.TASK_SETTING_UNKNOWN_DECLARATION, RobotTokenType.KEYWORD_SETTING_ARGUMENTS,
            RobotTokenType.KEYWORD_SETTING_DOCUMENTATION, RobotTokenType.KEYWORD_SETTING_TAGS,
            RobotTokenType.KEYWORD_SETTING_TEARDOWN, RobotTokenType.KEYWORD_SETTING_RETURN,
            RobotTokenType.KEYWORD_SETTING_TIMEOUT, RobotTokenType.KEYWORD_SETTING_UNKNOWN_DECLARATION);

    private static final long serialVersionUID = 1L;

    public RobotDefinitionSetting(final RobotCodeHoldingElement<?> robotCodeHoldingElement,
            final AModelElement<?> linkedElement) {
        super(robotCodeHoldingElement, linkedElement);
    }

    @Override
    public String getName() {
        final String nameInBrackets = super.getName();
        return nameInBrackets.substring(1, nameInBrackets.length() - 1);
    }

    @Override
    public List<String> getArguments() {
        if (arguments == null) {
            arguments = getLinkedElement().getElementTokens().stream().filter(token -> {
                final List<IRobotTokenType> types = token.getTypes();
                final IRobotTokenType type = types.isEmpty() ? null : types.get(0);
                return !NON_ARG_TYPES.contains(type);
            }).map(RobotToken::getText).collect(toList());
        }
        return arguments;
    }

    public boolean isArguments() {
        return getLinkedElement().getModelType() == ModelType.USER_KEYWORD_ARGUMENTS;
    }

    public boolean isDocumentation() {
        final ModelType modelType = getLinkedElement().getModelType();
        return modelType == ModelType.TEST_CASE_DOCUMENTATION || modelType == ModelType.TASK_DOCUMENTATION
                || modelType == ModelType.USER_KEYWORD_DOCUMENTATION;
    }

    public boolean isExecutableSetting() {
        final ModelType modelType = getLinkedElement().getModelType();
        return modelType == ModelType.TEST_CASE_SETUP || modelType == ModelType.TEST_CASE_TEARDOWN
                || modelType == ModelType.TASK_SETUP || modelType == ModelType.TASK_TEARDOWN
                || modelType == ModelType.USER_KEYWORD_TEARDOWN;
    }

    public ExecutableSetting getExecutableSetting() {
        if (isExecutableSetting()) {
            return ((LocalSetting<?>) getLinkedElement()).adaptTo(ExecutableSetting.class);
        }
        throw new IllegalStateException("Non-executable setting cannot be viewed as executable one");
    }

    public boolean isTemplate() {
        final ModelType modelType = getLinkedElement().getModelType();
        return modelType == ModelType.TEST_CASE_TEMPLATE || modelType == ModelType.TASK_TEMPLATE;
    }

    public boolean isTeardown() {
        final ModelType modelType = getLinkedElement().getModelType();
        return modelType == ModelType.TEST_CASE_TEARDOWN || modelType == ModelType.TASK_TEARDOWN
                || modelType == ModelType.USER_KEYWORD_TEARDOWN;
    }

    public boolean isTags() {
        final ModelType modelType = getLinkedElement().getModelType();
        return modelType == ModelType.TEST_CASE_TAGS || modelType == ModelType.TASK_TAGS
                || modelType == ModelType.USER_KEYWORD_TAGS;
    }
}
