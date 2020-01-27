/*
* Copyright 2016 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.robotframework.ide.eclipse.main.plugin.tableeditor.source.colouring;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

import org.eclipse.jface.text.rules.IToken;
import org.rf.ide.core.testdata.model.table.variables.descs.VariablesAnalyzer;
import org.rf.ide.core.testdata.text.read.IRobotLineElement;
import org.rf.ide.core.testdata.text.read.RobotLine;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;


public class SettingsTemplateRule extends VariableUsageRule {

    private static EnumSet<RobotTokenType> types = EnumSet.of(
            RobotTokenType.SETTING_TEST_TEMPLATE_KEYWORD_NAME,
            RobotTokenType.SETTING_TEST_TEMPLATE_KEYWORD_UNWANTED_ARGUMENT,
            RobotTokenType.SETTING_TASK_TEMPLATE_KEYWORD_NAME,
            RobotTokenType.SETTING_TASK_TEMPLATE_KEYWORD_UNWANTED_ARGUMENT,
            RobotTokenType.TEST_CASE_SETTING_TEMPLATE_KEYWORD_NAME,
            RobotTokenType.TEST_CASE_SETTING_TEMPLATE_KEYWORD_UNWANTED_ARGUMENT,
            RobotTokenType.TASK_SETTING_TEMPLATE_KEYWORD_NAME,
            RobotTokenType.TASK_SETTING_TEMPLATE_KEYWORD_UNWANTED_ARGUMENT);

    public SettingsTemplateRule(final IToken textToken, final IToken embeddedVariablesToken) {
        super(embeddedVariablesToken, textToken);
    }

    @Override
    public Optional<PositionedTextToken> evaluate(final IRobotLineElement token, final int offsetInToken,
            final List<RobotLine> context) {
        if (token.getText().equalsIgnoreCase("none")) {
            return Optional.empty();
        } else if (types.contains(token.getTypes().get(0))) {
            final Optional<PositionedTextToken> evaluated = super.evaluate(token, offsetInToken, context);
            if (evaluated.isPresent()) {
                return evaluated;
            }
            return Optional.of(new PositionedTextToken(nonVarToken, token.getStartOffset(), token.getText().length()));
        } else {
            return Optional.empty();
        }
    }

    @Override
    protected String getAllowedVariableMarks() {
        return VariablesAnalyzer.ALL_ROBOT;
    }
}
