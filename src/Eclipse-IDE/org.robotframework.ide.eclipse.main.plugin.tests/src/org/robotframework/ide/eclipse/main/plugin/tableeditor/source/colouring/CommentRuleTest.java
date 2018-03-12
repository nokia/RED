/*
* Copyright 2016 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.robotframework.ide.eclipse.main.plugin.tableeditor.source.colouring;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.Optional;
import java.util.Random;
import java.util.regex.Pattern;

import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.rules.Token;
import org.junit.Test;
import org.rf.ide.core.testdata.model.FilePosition;
import org.rf.ide.core.testdata.text.read.IRobotLineElement;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;
import org.rf.ide.core.testdata.text.read.separators.Separator;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.colouring.CommentRule.ITodoTaskToken;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.colouring.ISyntaxColouringRule.PositionedTextToken;

public class CommentRuleTest {

    private final CommentRule testedRule = new CommentRule(new Token("token"), new MockTasksToken("task", false, ".*"));

    @Test
    public void ruleIsApplicableOnlyForRobotTokens() {
        assertThat(testedRule.isApplicable(new RobotToken())).isTrue();
        assertThat(testedRule.isApplicable(new Separator())).isFalse();
        assertThat(testedRule.isApplicable(mock(IRobotLineElement.class))).isFalse();
    }

    @Test
    public void commentIsRecognized() {
        boolean thereWasName = false;
        for (final RobotToken token : TokensSource.createTokens()) {
            final Optional<PositionedTextToken> evaluatedToken = evaluate(token);

            if (token.getText().contains("comment")) {
                thereWasName = true;

                assertThat(evaluatedToken).isPresent();
                assertThat(evaluatedToken.get().getPosition())
                        .isEqualTo(new Position(token.getStartOffset(), token.getText().length()));
                assertThat(evaluatedToken.get().getToken().getData()).isEqualTo("token");

            } else {
                assertThat(evaluatedToken).isNotPresent();
            }
        }
        assertThat(thereWasName).isTrue();
    }

    @Test
    public void commentIsRecognized_evenWhenPositionIsInsideToken() {
        boolean thereWasName = false;
        for (final RobotToken token : TokensSource.createTokens()) {
            final int positionInsideToken = new Random().nextInt(Math.max(1, token.getText().length() - 1)) + 1;
            final Optional<PositionedTextToken> evaluatedToken = evaluate(token, positionInsideToken);

            if (token.getText().contains("comment")) {
                thereWasName = true;

                assertThat(evaluatedToken).isPresent();
                assertThat(evaluatedToken.get().getPosition())
                        .isEqualTo(new Position(token.getStartOffset(), token.getText().length()));
                assertThat(evaluatedToken.get().getToken().getData()).isEqualTo("token");

            } else {
                assertThat(evaluatedToken).isNotPresent();
            }
        }
        assertThat(thereWasName).isTrue();
    }

    @Test
    public void taskTagIsRecognizedInsideTokens() {
        final RobotToken token = RobotToken.create("# this is a comment with TODO task");
        token.setType(RobotTokenType.START_HASH_COMMENT);
        token.setFilePosition(new FilePosition(-1, -1, 100));

        final CommentRule rule = new CommentRule(new Token("token"), new MockTasksToken("task", true, "TODO"));

        for (int i = 0; i < 25; i++) {
            assertThat(rule.evaluate(token, i, new ArrayList<>()))
                    .contains(new PositionedTextToken(new Token("token"), 100 + i, 25 - i));
        }
        assertThat(rule.evaluate(token, 25, new ArrayList<>()))
                .contains(new PositionedTextToken(new Token("task"), 125, 4));
        for (int i = 26; i < token.getText().length(); i++) {
            assertThat(rule.evaluate(token, i, new ArrayList<>()))
                    .contains(new PositionedTextToken(new Token("token"), 100 + i, token.getText().length() - i));
        }
    }

    @Test
    public void multipleTaskTagsAreRecognizedInsideToken() {
        final RobotToken token = RobotToken.create("# this is a comment with TODO task and some FIXME tag");
        token.setType(RobotTokenType.START_HASH_COMMENT);
        token.setFilePosition(new FilePosition(-1, -1, 100));

        final CommentRule rule = new CommentRule(new Token("token"), new MockTasksToken("task", true, "TODO|FIXME"));

        for (int i = 0; i < 25; i++) {
            assertThat(rule.evaluate(token, i, new ArrayList<>()))
                    .contains(new PositionedTextToken(new Token("token"), 100 + i, 25 - i));
        }
        assertThat(rule.evaluate(token, 25, new ArrayList<>()))
                .contains(new PositionedTextToken(new Token("task"), 125, 4));
        for (int i = 26; i < 44; i++) {
            assertThat(rule.evaluate(token, i, new ArrayList<>()))
                    .contains(new PositionedTextToken(new Token("token"), 100 + i, 44 - i));
        }
        assertThat(rule.evaluate(token, 44, new ArrayList<>()))
                .contains(new PositionedTextToken(new Token("task"), 144, 5));
        for (int i = 45; i < token.getText().length(); i++) {
            assertThat(rule.evaluate(token, i, new ArrayList<>()))
                    .contains(new PositionedTextToken(new Token("token"), 100 + i, token.getText().length() - i));
        }
    }

    private Optional<PositionedTextToken> evaluate(final RobotToken token) {
        return evaluate(token, 0);
    }

    private Optional<PositionedTextToken> evaluate(final RobotToken token, final int position) {
        return testedRule.evaluate(token, position, new ArrayList<>());
    }

    private static class MockTasksToken extends Token implements ITodoTaskToken {

        private final boolean enabled;

        private final Pattern pattern;

        public MockTasksToken(final String data, final boolean isEnabled, final String pattern) {
            super(data);
            this.enabled = isEnabled;
            this.pattern = Pattern.compile(pattern);
        }

        @Override
        public boolean isTaskDetectionEnabled() {
            return enabled;
        }

        @Override
        public Pattern getTasksPattern() {
            return pattern;
        }
    }
}
