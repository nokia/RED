/*
* Copyright 2016 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.robotframework.ide.eclipse.main.plugin.tableeditor.source.colouring;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.rules.Token;
import org.junit.Test;
import org.rf.ide.core.testdata.text.read.IRobotLineElement;
import org.rf.ide.core.testdata.text.read.RobotLine;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;
import org.rf.ide.core.testdata.text.read.separators.Separator;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.colouring.ISyntaxColouringRule.PositionedTextToken;

public class GherkinPrefixRuleTest {

    private final GherkinPrefixRule testedRule = GherkinPrefixRule.forExecutableInTestCase(new Token("token"));

    @Test
    public void ruleIsApplicableOnlyForRobotTokens() {
        final RobotToken caseAction1 = RobotToken.create("", newArrayList(RobotTokenType.TEST_CASE_ACTION_NAME));
        final RobotToken caseAction2 = RobotToken.create("", newArrayList(RobotTokenType.TEST_CASE_ACTION_ARGUMENT));
        final RobotToken kwAction1 = RobotToken.create("", newArrayList(RobotTokenType.KEYWORD_ACTION_NAME));
        final RobotToken kwAction2 = RobotToken.create("", newArrayList(RobotTokenType.KEYWORD_ACTION_ARGUMENT));
        final RobotToken settingAction1 = RobotToken.create("", newArrayList(RobotTokenType.SETTING_SUITE_SETUP_KEYWORD_NAME));
        final RobotToken settingAction2 = RobotToken.create("", newArrayList(RobotTokenType.SETTING_SUITE_SETUP_KEYWORD_NAME));
        final RobotToken settingAction3 = RobotToken.create("", newArrayList(RobotTokenType.SETTING_SUITE_SETUP_KEYWORD_NAME));
        final RobotToken settingAction4 = RobotToken.create("", newArrayList(RobotTokenType.SETTING_SUITE_SETUP_KEYWORD_NAME));
        final RobotToken settingAction5 = RobotToken.create("", newArrayList(RobotTokenType.SETTING_SUITE_SETUP_KEYWORD_NAME));
        final RobotToken settingAction6 = RobotToken.create("", newArrayList(RobotTokenType.SETTING_SUITE_SETUP_KEYWORD_NAME));
        final RobotToken tcSettingAction1 = RobotToken.create("", newArrayList(RobotTokenType.TEST_CASE_SETTING_SETUP_KEYWORD_NAME));
        final RobotToken tcSettingAction2 = RobotToken.create("", newArrayList(RobotTokenType.TEST_CASE_SETTING_SETUP_KEYWORD_ARGUMENT));
        final RobotToken tcSettingAction3 = RobotToken.create("", newArrayList(RobotTokenType.TEST_CASE_SETTING_TEARDOWN_KEYWORD_NAME));
        final RobotToken tcSettingAction4 = RobotToken.create("", newArrayList(RobotTokenType.TEST_CASE_SETTING_TEARDOWN_KEYWORD_ARGUMENT));
        final RobotToken kwSettingAction1 = RobotToken.create("", newArrayList(RobotTokenType.KEYWORD_SETTING_TEARDOWN_KEYWORD_NAME));
        final RobotToken kwSettingAction2 = RobotToken.create("", newArrayList(RobotTokenType.KEYWORD_SETTING_TEARDOWN_KEYWORD_ARGUMENT));
        
        assertThat(GherkinPrefixRule.forExecutableInTestCase(new Token("token")).isApplicable(caseAction1)).isTrue();
        assertThat(GherkinPrefixRule.forExecutableInTestCase(new Token("token")).isApplicable(caseAction2)).isTrue();
        assertThat(GherkinPrefixRule.forExecutableInTestCase(new Token("token")).isApplicable(kwAction1)).isFalse();
        assertThat(GherkinPrefixRule.forExecutableInTestCase(new Token("token")).isApplicable(kwAction2)).isFalse();
        assertThat(GherkinPrefixRule.forExecutableInTestCase(new Token("token")).isApplicable(settingAction1)).isFalse();
        assertThat(GherkinPrefixRule.forExecutableInTestCase(new Token("token")).isApplicable(settingAction2)).isFalse();
        assertThat(GherkinPrefixRule.forExecutableInTestCase(new Token("token")).isApplicable(settingAction3)).isFalse();
        assertThat(GherkinPrefixRule.forExecutableInTestCase(new Token("token")).isApplicable(settingAction4)).isFalse();
        assertThat(GherkinPrefixRule.forExecutableInTestCase(new Token("token")).isApplicable(settingAction5)).isFalse();
        assertThat(GherkinPrefixRule.forExecutableInTestCase(new Token("token")).isApplicable(settingAction6)).isFalse();
        assertThat(GherkinPrefixRule.forExecutableInTestCase(new Token("token")).isApplicable(tcSettingAction1)).isFalse();
        assertThat(GherkinPrefixRule.forExecutableInTestCase(new Token("token")).isApplicable(tcSettingAction2)).isFalse();
        assertThat(GherkinPrefixRule.forExecutableInTestCase(new Token("token")).isApplicable(tcSettingAction3)).isFalse();
        assertThat(GherkinPrefixRule.forExecutableInTestCase(new Token("token")).isApplicable(tcSettingAction4)).isFalse();
        assertThat(GherkinPrefixRule.forExecutableInTestCase(new Token("token")).isApplicable(kwSettingAction1)).isFalse();
        assertThat(GherkinPrefixRule.forExecutableInTestCase(new Token("token")).isApplicable(kwSettingAction2)).isFalse();
        
        assertThat(GherkinPrefixRule.forExecutableInKeyword(new Token("token")).isApplicable(caseAction1)).isFalse();
        assertThat(GherkinPrefixRule.forExecutableInKeyword(new Token("token")).isApplicable(caseAction2)).isFalse();
        assertThat(GherkinPrefixRule.forExecutableInKeyword(new Token("token")).isApplicable(kwAction1)).isTrue();
        assertThat(GherkinPrefixRule.forExecutableInKeyword(new Token("token")).isApplicable(kwAction2)).isTrue();
        assertThat(GherkinPrefixRule.forExecutableInKeyword(new Token("token")).isApplicable(settingAction1)).isFalse();
        assertThat(GherkinPrefixRule.forExecutableInKeyword(new Token("token")).isApplicable(settingAction2)).isFalse();
        assertThat(GherkinPrefixRule.forExecutableInKeyword(new Token("token")).isApplicable(settingAction3)).isFalse();
        assertThat(GherkinPrefixRule.forExecutableInKeyword(new Token("token")).isApplicable(settingAction4)).isFalse();
        assertThat(GherkinPrefixRule.forExecutableInKeyword(new Token("token")).isApplicable(settingAction5)).isFalse();
        assertThat(GherkinPrefixRule.forExecutableInKeyword(new Token("token")).isApplicable(settingAction6)).isFalse();
        assertThat(GherkinPrefixRule.forExecutableInKeyword(new Token("token")).isApplicable(tcSettingAction1)).isFalse();
        assertThat(GherkinPrefixRule.forExecutableInKeyword(new Token("token")).isApplicable(tcSettingAction2)).isFalse();
        assertThat(GherkinPrefixRule.forExecutableInKeyword(new Token("token")).isApplicable(tcSettingAction3)).isFalse();
        assertThat(GherkinPrefixRule.forExecutableInKeyword(new Token("token")).isApplicable(tcSettingAction4)).isFalse();
        assertThat(GherkinPrefixRule.forExecutableInKeyword(new Token("token")).isApplicable(kwSettingAction1)).isFalse();
        assertThat(GherkinPrefixRule.forExecutableInKeyword(new Token("token")).isApplicable(kwSettingAction2)).isFalse();

        assertThat(GherkinPrefixRule.forExecutableInSetting(new Token("token")).isApplicable(caseAction1)).isFalse();
        assertThat(GherkinPrefixRule.forExecutableInSetting(new Token("token")).isApplicable(caseAction2)).isFalse();
        assertThat(GherkinPrefixRule.forExecutableInSetting(new Token("token")).isApplicable(kwAction1)).isFalse();
        assertThat(GherkinPrefixRule.forExecutableInSetting(new Token("token")).isApplicable(kwAction2)).isFalse();
        assertThat(GherkinPrefixRule.forExecutableInSetting(new Token("token")).isApplicable(settingAction1)).isTrue();
        assertThat(GherkinPrefixRule.forExecutableInSetting(new Token("token")).isApplicable(settingAction2)).isTrue();
        assertThat(GherkinPrefixRule.forExecutableInSetting(new Token("token")).isApplicable(settingAction3)).isTrue();
        assertThat(GherkinPrefixRule.forExecutableInSetting(new Token("token")).isApplicable(settingAction4)).isTrue();
        assertThat(GherkinPrefixRule.forExecutableInSetting(new Token("token")).isApplicable(settingAction5)).isTrue();
        assertThat(GherkinPrefixRule.forExecutableInSetting(new Token("token")).isApplicable(settingAction6)).isTrue();
        assertThat(GherkinPrefixRule.forExecutableInSetting(new Token("token")).isApplicable(tcSettingAction1)).isFalse();
        assertThat(GherkinPrefixRule.forExecutableInSetting(new Token("token")).isApplicable(tcSettingAction2)).isFalse();
        assertThat(GherkinPrefixRule.forExecutableInSetting(new Token("token")).isApplicable(tcSettingAction3)).isFalse();
        assertThat(GherkinPrefixRule.forExecutableInSetting(new Token("token")).isApplicable(tcSettingAction4)).isFalse();
        assertThat(GherkinPrefixRule.forExecutableInSetting(new Token("token")).isApplicable(kwSettingAction1)).isFalse();
        assertThat(GherkinPrefixRule.forExecutableInSetting(new Token("token")).isApplicable(kwSettingAction2)).isFalse();
        
        assertThat(GherkinPrefixRule.forExecutableInTestCaseSetting(new Token("token")).isApplicable(caseAction1)).isFalse();
        assertThat(GherkinPrefixRule.forExecutableInTestCaseSetting(new Token("token")).isApplicable(caseAction2)).isFalse();
        assertThat(GherkinPrefixRule.forExecutableInTestCaseSetting(new Token("token")).isApplicable(kwAction1)).isFalse();
        assertThat(GherkinPrefixRule.forExecutableInTestCaseSetting(new Token("token")).isApplicable(kwAction2)).isFalse();
        assertThat(GherkinPrefixRule.forExecutableInTestCaseSetting(new Token("token")).isApplicable(settingAction1)).isFalse();
        assertThat(GherkinPrefixRule.forExecutableInTestCaseSetting(new Token("token")).isApplicable(settingAction2)).isFalse();
        assertThat(GherkinPrefixRule.forExecutableInTestCaseSetting(new Token("token")).isApplicable(settingAction3)).isFalse();
        assertThat(GherkinPrefixRule.forExecutableInTestCaseSetting(new Token("token")).isApplicable(settingAction4)).isFalse();
        assertThat(GherkinPrefixRule.forExecutableInTestCaseSetting(new Token("token")).isApplicable(settingAction5)).isFalse();
        assertThat(GherkinPrefixRule.forExecutableInTestCaseSetting(new Token("token")).isApplicable(settingAction6)).isFalse();
        assertThat(GherkinPrefixRule.forExecutableInTestCaseSetting(new Token("token")).isApplicable(tcSettingAction1)).isTrue();
        assertThat(GherkinPrefixRule.forExecutableInTestCaseSetting(new Token("token")).isApplicable(tcSettingAction2)).isTrue();
        assertThat(GherkinPrefixRule.forExecutableInTestCaseSetting(new Token("token")).isApplicable(tcSettingAction3)).isTrue();
        assertThat(GherkinPrefixRule.forExecutableInTestCaseSetting(new Token("token")).isApplicable(tcSettingAction4)).isTrue();
        assertThat(GherkinPrefixRule.forExecutableInTestCaseSetting(new Token("token")).isApplicable(kwSettingAction1)).isFalse();
        assertThat(GherkinPrefixRule.forExecutableInTestCaseSetting(new Token("token")).isApplicable(kwSettingAction2)).isFalse();
        
        assertThat(GherkinPrefixRule.forExecutableInKeywordSetting(new Token("token")).isApplicable(caseAction1)).isFalse();
        assertThat(GherkinPrefixRule.forExecutableInKeywordSetting(new Token("token")).isApplicable(caseAction2)).isFalse();
        assertThat(GherkinPrefixRule.forExecutableInKeywordSetting(new Token("token")).isApplicable(kwAction1)).isFalse();
        assertThat(GherkinPrefixRule.forExecutableInKeywordSetting(new Token("token")).isApplicable(kwAction2)).isFalse();
        assertThat(GherkinPrefixRule.forExecutableInKeywordSetting(new Token("token")).isApplicable(settingAction1)).isFalse();
        assertThat(GherkinPrefixRule.forExecutableInKeywordSetting(new Token("token")).isApplicable(settingAction2)).isFalse();
        assertThat(GherkinPrefixRule.forExecutableInKeywordSetting(new Token("token")).isApplicable(settingAction3)).isFalse();
        assertThat(GherkinPrefixRule.forExecutableInKeywordSetting(new Token("token")).isApplicable(settingAction4)).isFalse();
        assertThat(GherkinPrefixRule.forExecutableInKeywordSetting(new Token("token")).isApplicable(settingAction5)).isFalse();
        assertThat(GherkinPrefixRule.forExecutableInKeywordSetting(new Token("token")).isApplicable(settingAction6)).isFalse();
        assertThat(GherkinPrefixRule.forExecutableInKeywordSetting(new Token("token")).isApplicable(tcSettingAction1)).isFalse();
        assertThat(GherkinPrefixRule.forExecutableInKeywordSetting(new Token("token")).isApplicable(tcSettingAction2)).isFalse();
        assertThat(GherkinPrefixRule.forExecutableInKeywordSetting(new Token("token")).isApplicable(tcSettingAction3)).isFalse();
        assertThat(GherkinPrefixRule.forExecutableInKeywordSetting(new Token("token")).isApplicable(tcSettingAction4)).isFalse();
        assertThat(GherkinPrefixRule.forExecutableInKeywordSetting(new Token("token")).isApplicable(kwSettingAction1)).isTrue();
        assertThat(GherkinPrefixRule.forExecutableInKeywordSetting(new Token("token")).isApplicable(kwSettingAction2)).isTrue();

        assertThat(testedRule.isApplicable(new RobotToken())).isFalse();
        assertThat(testedRule.isApplicable(new Separator())).isFalse();
        assertThat(testedRule.isApplicable(mock(IRobotLineElement.class))).isFalse();
    }

    @Test
    public void gherkinPrefixIsRecognized() {
        boolean thereWasName = false;
        final List<RobotLine> lines = TokensSource.createTokensInLines();
        for (final RobotLine line : lines) {
            for (final IRobotLineElement token : line.getLineElements()) {
                final Optional<PositionedTextToken> evaluatedToken = testedRule.evaluate(token, 0, lines);

                if (token.getText().contains("gherkin_call")) {
                    thereWasName = true;

                    assertThat(evaluatedToken).isPresent();
                    assertThat(evaluatedToken.get().getPosition()).isEqualTo(
                            new Position(token.getStartOffset(), token.getText().length() - "gherkin_call".length()));
                    assertThat(evaluatedToken.get().getToken().getData()).isEqualTo("token");

                } else {
                    assertThat(evaluatedToken).isNotPresent();
                }
            }
        }
        assertThat(thereWasName).isTrue();
    }

    @Test
    public void gherkinPrefixIsNotRecognized_whenPositionIsInsideToken() {
        final List<IRobotLineElement> previousTokens = new ArrayList<>();

        boolean thereWasName = false;
        for (final RobotToken token : TokensSource.createTokens()) {
            final int positionInsideToken = new Random().nextInt(token.getText().length()) + 1;
            final Optional<PositionedTextToken> evaluatedToken = testedRule.evaluate(token, positionInsideToken,
                    createLines(previousTokens));

            thereWasName = true;

            assertThat(evaluatedToken).isNotPresent();
            previousTokens.add(token);
        }
        assertThat(thereWasName).isTrue();
    }

    private List<RobotLine> createLines(final List<IRobotLineElement> previousTokens) {
        final RobotLine line = new RobotLine(0, null);
        line.getLineElements().addAll(previousTokens);
        return newArrayList(line);
    }
}
