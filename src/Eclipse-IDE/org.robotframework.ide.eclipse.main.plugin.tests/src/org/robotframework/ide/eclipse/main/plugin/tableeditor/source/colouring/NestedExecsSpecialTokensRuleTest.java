/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.source.colouring;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;
import java.util.Random;

import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.rules.Token;
import org.junit.Test;
import org.rf.ide.core.testdata.text.read.IRobotLineElement;
import org.rf.ide.core.testdata.text.read.RobotLine;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.colouring.ISyntaxColouringRule.PositionedTextToken;


public class NestedExecsSpecialTokensRuleTest {

    @Test
    public void specialWordsInOrdinaryKeywordsAreNotRecognized() {
        boolean thereAreSpecialWords = false;
        final List<RobotLine> lines = TokensSource.createTokensInLines();
        for (final RobotLine line : lines) {
            for (final IRobotLineElement token : line.getLineElements()) {
                final int positionInsideToken = new Random().nextInt(token.getText().length());
                final Optional<PositionedTextToken> evaluatedToken = evaluate(token, positionInsideToken, lines);

                if (token.getText().equals("AND") || token.getText().equals("ELSE")
                        || token.getText().equals("ELSE IF")) {
                    thereAreSpecialWords = true;

                }
                assertThat(evaluatedToken).isNotPresent();
            }
        }
        assertThat(thereAreSpecialWords).isTrue();
    }

    @Test
    public void specialWordsInNestingKeywordsAreRecognized() {
        boolean thereAreSpecialWords = false;
        final List<RobotLine> lines = TokensSource.createTokensInLinesWithSpecialNestingKeywords();
        for (final RobotLine line : lines) {
            for (final IRobotLineElement token : line.getLineElements()) {
                final int positionInsideToken = new Random().nextInt(token.getText().length());
                final Optional<PositionedTextToken> evaluatedToken = evaluate(token, positionInsideToken, lines);

                if (token.getText().equals("AND") || token.getText().equals("ELSE")
                        || token.getText().equals("ELSE IF")) {
                    thereAreSpecialWords = true;

                    assertThat(evaluatedToken).isPresent();
                    assertThat(evaluatedToken.get().getPosition())
                            .isEqualTo(new Position(token.getStartOffset(), token.getText().length()));
                    assertThat(evaluatedToken.get().getToken().getData()).isEqualTo("special_token");

                } else {
                    assertThat(evaluatedToken).isNotPresent();
                }
            }
        }
        assertThat(thereAreSpecialWords).isTrue();
    }

    private Optional<PositionedTextToken> evaluate(final IRobotLineElement token, final int position,
            final List<RobotLine> lines) {
        final NestedExecsSpecialTokensRule rule = new NestedExecsSpecialTokensRule(new Token("special_token"));
        return rule.isApplicable(token) ? rule.evaluate(token, position, lines) : Optional.empty();
    }
}
