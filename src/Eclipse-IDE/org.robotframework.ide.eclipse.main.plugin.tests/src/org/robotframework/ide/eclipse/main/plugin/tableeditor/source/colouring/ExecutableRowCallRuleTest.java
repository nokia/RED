/*
* Copyright 2016 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.robotframework.ide.eclipse.main.plugin.tableeditor.source.colouring;

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
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.separators.Separator;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.colouring.ISyntaxColouringRule.PositionedTextToken;

public class ExecutableRowCallRuleTest {

    private final ExecutableRowCallRule testedRule = new ExecutableRowCallRule(new Token("token"));

    @Test
    public void ruleIsApplicableOnlyForRobotTokens() {
        assertThat(testedRule.isApplicable(new RobotToken())).isTrue();
        assertThat(testedRule.isApplicable(new Separator())).isFalse();
        assertThat(testedRule.isApplicable(mock(IRobotLineElement.class))).isFalse();
    }

    @Test
    public void executableCallIsRecognized() {
        final List<IRobotLineElement> previousTokens = new ArrayList<>();

        boolean thereWasName = false;
        for (final RobotToken token : TokensSource.createTokens()) {
            final Optional<PositionedTextToken> evaluatedToken = testedRule.evaluate(token, 0,
                    previousTokens);

            if (token.getText().equals("call") || token.getText().equals(":FOR") || token.getText().equals("\\")
                    || token.getText().equals("given call") || token.getText().equals("when then call")) {
                thereWasName = true;

                assertThat(evaluatedToken).isPresent();
                assertThat(evaluatedToken.get().getPosition())
                        .isEqualTo(new Position(token.getStartOffset(), token.getText().length()));
                assertThat(evaluatedToken.get().getToken().getData()).isEqualTo("token");

            } else {
                assertThat(evaluatedToken).isNotPresent();
            }
            previousTokens.add(token);
        }
        assertThat(thereWasName).isTrue();
    }

    @Test
    public void executableCallIsRecognized_evenWhenPositionIsInsideToken() {
        final List<IRobotLineElement> previousTokens = new ArrayList<>();

        boolean thereWasName = false;
        for (final RobotToken token : TokensSource.createTokens()) {
            final int positionInsideToken = new Random().nextInt(token.getText().length());
            final Optional<PositionedTextToken> evaluatedToken = testedRule.evaluate(token, positionInsideToken,
                    previousTokens);

            if (token.getText().equals("call") || token.getText().equals(":FOR") || token.getText().equals("\\")
                    || token.getText().equals("given call") || token.getText().equals("when then call")) {
                thereWasName = true;

                assertThat(evaluatedToken).isPresent();
                assertThat(evaluatedToken.get().getPosition()).isEqualTo(new Position(
                        token.getStartOffset() + positionInsideToken, token.getText().length() - positionInsideToken));
                assertThat(evaluatedToken.get().getToken().getData()).isEqualTo("token");

            } else {
                assertThat(evaluatedToken).isNotPresent();
            }
            previousTokens.add(token);
        }
        assertThat(thereWasName).isTrue();
    }
}
