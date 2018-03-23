/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model;

import static java.util.stream.Collectors.toList;

import java.util.List;

import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.ModelType;
import org.rf.ide.core.testdata.text.read.IRobotTokenType;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

public class RobotDefinitionSetting extends RobotKeywordCall {

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
                return type != RobotTokenType.START_HASH_COMMENT && type != RobotTokenType.COMMENT_CONTINUE
                        && type != RobotTokenType.TEST_CASE_SETTING_SETUP
                        && type != RobotTokenType.TEST_CASE_SETTING_DOCUMENTATION
                        && type != RobotTokenType.TEST_CASE_SETTING_TAGS_DECLARATION
                        && type != RobotTokenType.TEST_CASE_SETTING_TEARDOWN
                        && type != RobotTokenType.TEST_CASE_SETTING_TEMPLATE
                        && type != RobotTokenType.TEST_CASE_SETTING_TIMEOUT
                        && type != RobotTokenType.TEST_CASE_SETTING_UNKNOWN_DECLARATION
                        && type != RobotTokenType.KEYWORD_SETTING_ARGUMENTS
                        && type != RobotTokenType.KEYWORD_SETTING_DOCUMENTATION
                        && type != RobotTokenType.KEYWORD_SETTING_TAGS
                        && type != RobotTokenType.KEYWORD_SETTING_TEARDOWN
                        && type != RobotTokenType.KEYWORD_SETTING_RETURN
                        && type != RobotTokenType.KEYWORD_SETTING_TIMEOUT
                        && type != RobotTokenType.KEYWORD_SETTING_UNKNOWN_DECLARATION;
            }).map(RobotToken::getText).collect(toList());
        }
        return arguments;
    }

    @Override
    public boolean shouldAddCommentMark() {
        final RobotToken declaration = getLinkedElement().getDeclaration();
        if (declaration.isDirty()) {
            if (declaration.getText().isEmpty()) {
                return true;
            } else {
                return !declaration.getText().trim().startsWith("#");
            }
        } else {
            return !declaration.getTypes().contains(RobotTokenType.START_HASH_COMMENT);
        }
    }

    public boolean isArguments() {
        return getLinkedElement().getModelType() == ModelType.USER_KEYWORD_ARGUMENTS;
    }

    public boolean isDocumentation() {
        final ModelType modelType = getLinkedElement().getModelType();
        return modelType == ModelType.TEST_CASE_DOCUMENTATION || modelType == ModelType.USER_KEYWORD_DOCUMENTATION;
    }

    public boolean isKeywordBased() {
        final ModelType modelType = getLinkedElement().getModelType();
        return modelType == ModelType.TEST_CASE_SETUP || modelType == ModelType.TEST_CASE_TEARDOWN
                || modelType == ModelType.TEST_CASE_TEMPLATE || modelType == ModelType.USER_KEYWORD_TEARDOWN;
    }

    public boolean isTeardown() {
        final ModelType modelType = getLinkedElement().getModelType();
        return modelType == ModelType.TEST_CASE_TEARDOWN || modelType == ModelType.USER_KEYWORD_TEARDOWN;
    }

    @Override
    public RobotKeywordCall insertCellAt(final int position, final String newValue) {
        if (position > 0) {
            return super.insertCellAt(position, newValue);
        }
        final RobotCodeHoldingElement<?> parent = (RobotCodeHoldingElement<?>) getParent();
        final int index = getIndex();
        parent.removeChild(this);
        final List<String> args = getArguments();
        args.add(0, super.getName());
        final RobotKeywordCall resultCall = parent.createKeywordCall(index, "", args, getComment());
        resultCall.resetStored();
        return resultCall;
    }
}
