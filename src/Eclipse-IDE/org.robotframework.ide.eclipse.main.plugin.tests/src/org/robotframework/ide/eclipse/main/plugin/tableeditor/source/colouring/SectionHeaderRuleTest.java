/*
* Copyright 2016 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.robotframework.ide.eclipse.main.plugin.tableeditor.source.colouring;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.rules.Token;
import org.junit.jupiter.api.Test;
import org.rf.ide.core.testdata.text.read.IRobotLineElement;
import org.rf.ide.core.testdata.text.read.RobotLine;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.separators.Separator;
import org.robotframework.ide.eclipse.main.plugin.mockmodel.RobotSuiteFileCreator;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.colouring.ISyntaxColouringRule.PositionedTextToken;

public class SectionHeaderRuleTest {

    private final SectionHeaderRule testedRule = new SectionHeaderRule(new Token("token"));

    @Test
    public void ruleIsApplicableOnlyForRobotTokens() {
        assertThat(testedRule.isApplicable(new RobotToken())).isTrue();
        assertThat(testedRule.isApplicable(new Separator())).isFalse();
        assertThat(testedRule.isApplicable(mock(IRobotLineElement.class))).isFalse();
    }

    @Test
    public void sectionHeadersAreRecognized() {
        boolean thereWasName = false;
        for (final RobotToken token : TokensSource.createTokens()) {
            final Optional<PositionedTextToken> evaluatedToken = evaluate(token);

            if (token.getText().contains("*")) {
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
    public void sectionHeadersAreRecognized_evenWhenPositionIsInsideToken() {
        boolean thereWasName = false;
        for (final RobotToken token : TokensSource.createTokens()) {
            final int positionInsideToken = new Random().nextInt(token.getText().length());
            final Optional<PositionedTextToken> evaluatedToken = evaluate(token, positionInsideToken);

            if (token.getText().contains("*")) {
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
    public void sectionHeadersAreRecognized_EvenWhenFollowedBySingleSpace() {
        final List<RobotToken> headers = headersWithSpaces();
        assertThat(headers).isNotEmpty();

        for (final RobotToken token : headers) {
            final int positionInsideToken = new Random().nextInt(token.getText().length());
            final Optional<PositionedTextToken> evaluatedToken = evaluate(token, positionInsideToken);

            assertThat(evaluatedToken).isPresent();
            assertThat(evaluatedToken.get().getPosition())
                    .isEqualTo(new Position(token.getStartOffset(), token.getText().length()));
            assertThat(evaluatedToken.get().getToken().getData()).isEqualTo("token");
        }
    }

    private Optional<PositionedTextToken> evaluate(final RobotToken token) {
        return evaluate(token, 0);
    }

    private Optional<PositionedTextToken> evaluate(final RobotToken token, final int position) {
        return testedRule.evaluate(token, position, new ArrayList<>());
    }

    private static List<RobotToken> headersWithSpaces() {
        final RobotSuiteFile model = new RobotSuiteFileCreator()
                .appendLine("*** Test Cases *** ")
                .appendLine("*** Keywords *** ")
                .appendLine("*** Variables *** ")
                .appendLine("*** Settings *** ")
                .appendLine("*** unknown section *** ")
                .build();
        final List<RobotLine> lines = model.getLinkedElement().getFileContent();
        return lines.stream()
                .flatMap(line -> line.getLineElements().stream())
                .filter(RobotToken.class::isInstance)
                .map(RobotToken.class::cast)
                .filter(token -> token.getText().contains("*"))
                .collect(toList());
    }
}
