/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.source.colouring;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Stream;

import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.rules.Token;
import org.junit.jupiter.api.Test;
import org.rf.ide.core.testdata.text.read.IRobotLineElement;
import org.rf.ide.core.testdata.text.read.RobotLine;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.colouring.ISyntaxColouringRule.PositionedTextToken;


public class ExecutableCallInSettingsRuleTest {

    @Test
    public void executableSettingsCallsAreNotRecognized_whenTheyAreCalledNone() {
        final List<RobotLine> lines = TokensSource.createTokensOfNoneAwareSettings();
        final List<PositionedTextToken> coloredTokens = lines
                .stream()
                .map(RobotLine::getLineElements)
                .flatMap(List::stream)
                .filter(RobotToken.class::isInstance)
                .map(RobotToken.class::cast)
                .map(token -> evaluate(token, 0, lines))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(toList());

        assertThat(coloredTokens).isEmpty();
    }

    @Test
    public void nestedKeywordsAreRecognized_inAllSettings() {
        boolean thereAreNestedKw = false;
        final List<RobotLine> lines = TokensSource.createTokensInLinesWithSpecialNestingKeywords();
        for (final RobotLine line : lines) {
            for (final IRobotLineElement token : line.getLineElements()) {
                final int positionInsideToken = new Random().nextInt(token.getText().length());
                final Optional<PositionedTextToken> evaluatedToken = evaluate(token, positionInsideToken, lines);

                if (token.getText().equals("settings_nestedkw")) {
                    thereAreNestedKw = true;

                    assertThat(evaluatedToken).isPresent();
                    assertThat(evaluatedToken.get().getPosition())
                            .isEqualTo(new Position(token.getStartOffset() + positionInsideToken,
                                    token.getText().length() - positionInsideToken));
                    assertThat(evaluatedToken.get().getToken().getData()).isEqualTo("call_token");

                } else if (!token.getText().equals("Run Keyword If") && !token.getText().equals("Run Keywords")) {
                    assertThat(evaluatedToken).isNotPresent();
                }
            }
        }
        assertThat(thereAreNestedKw).isTrue();
    }

    private Optional<PositionedTextToken> evaluate(final IRobotLineElement token, final int position,
            final List<RobotLine> lines) {
        return Stream.of(
                ExecutableCallInSettingsRule.forExecutableInTestSetupOrTeardown(new Token("call_token"),
                        new Token("gherkin_token"), new Token("lib_token"), new Token("quote_token"), new Token("var_token")),
                ExecutableCallInSettingsRule.forExecutableInTaskSetupOrTeardown(new Token("call_token"),
                        new Token("gherkin_token"), new Token("lib_token"), new Token("quote_token"), new Token("var_token")),
                ExecutableCallInSettingsRule.forExecutableInKeywordTeardown(new Token("call_token"),
                        new Token("gherkin_token"), new Token("lib_token"), new Token("quote_token"), new Token("var_token")),
                ExecutableCallInSettingsRule.forExecutableInGeneralSettingsSetupsOrTeardowns(new Token("call_token"),
                        new Token("gherkin_token"), new Token("lib_token"), new Token("quote_token"), new Token("var_token")))
                .filter(rule -> rule.isApplicable(token))
                .findFirst()
                .flatMap(rule -> rule.evaluate(token, position, lines));
    }
}
