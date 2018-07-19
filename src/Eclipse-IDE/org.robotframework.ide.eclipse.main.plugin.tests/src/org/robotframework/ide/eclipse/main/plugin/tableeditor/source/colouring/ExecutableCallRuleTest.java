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
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Stream;

import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.rules.Token;
import org.junit.Test;
import org.rf.ide.core.testdata.text.read.IRobotLineElement;
import org.rf.ide.core.testdata.text.read.RobotLine;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;
import org.rf.ide.core.testdata.text.read.separators.Separator;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.colouring.ISyntaxColouringRule.PositionedTextToken;

public class ExecutableCallRuleTest {

    private final ExecutableCallRule testedRule = ExecutableCallRule.forExecutableInTestCase(new Token("call_token"),
            new Token("var_token"));

    @Test
    public void ruleIsApplicableOnlyForRobotTokens() {
        final RobotToken caseAction1 = RobotToken.create("", newArrayList(RobotTokenType.TEST_CASE_ACTION_NAME));
        final RobotToken caseAction2 = RobotToken.create("", newArrayList(RobotTokenType.TEST_CASE_ACTION_ARGUMENT));
        final RobotToken kwAction1 = RobotToken.create("", newArrayList(RobotTokenType.KEYWORD_ACTION_NAME));
        final RobotToken kwAction2 = RobotToken.create("", newArrayList(RobotTokenType.KEYWORD_ACTION_ARGUMENT));

        assertThat(ExecutableCallRule.forExecutableInTestCase(new Token("call_token"), new Token("var_token"))
                .isApplicable(caseAction1)).isTrue();
        assertThat(ExecutableCallRule.forExecutableInTestCase(new Token("call_token"), new Token("var_token"))
                .isApplicable(caseAction2)).isTrue();
        assertThat(ExecutableCallRule.forExecutableInTestCase(new Token("call_token"), new Token("var_token"))
                .isApplicable(kwAction1)).isFalse();
        assertThat(ExecutableCallRule.forExecutableInTestCase(new Token("call_token"), new Token("var_token"))
                .isApplicable(kwAction2)).isFalse();

        assertThat(ExecutableCallRule.forExecutableInKeyword(new Token("call_token"), new Token("var_token"))
                .isApplicable(caseAction1)).isFalse();
        assertThat(ExecutableCallRule.forExecutableInKeyword(new Token("call_token"), new Token("var_token"))
                .isApplicable(caseAction2)).isFalse();
        assertThat(ExecutableCallRule.forExecutableInKeyword(new Token("call_token"), new Token("var_token"))
                .isApplicable(kwAction1)).isTrue();
        assertThat(ExecutableCallRule.forExecutableInKeyword(new Token("call_token"), new Token("var_token"))
                .isApplicable(kwAction2)).isTrue();

        assertThat(testedRule.isApplicable(new RobotToken())).isFalse();
        assertThat(testedRule.isApplicable(new Separator())).isFalse();
        assertThat(testedRule.isApplicable(mock(IRobotLineElement.class))).isFalse();
    }

    @Test
    public void executableCallIsRecognized() {
        final List<IRobotLineElement> previousTokens = new ArrayList<>();

        boolean thereWasName = false;
        final List<RobotLine> lines = TokensSource.createTokensInLines();
        for (final RobotLine line : lines) {
            for (final IRobotLineElement token : line.getLineElements()) {
                final Optional<PositionedTextToken> evaluatedToken = evaluate(token, lines);

                if (token.getText().equals("call") || token.getText().equals(":FOR") || token.getText().equals("\\")
                        || token.getText().contains("gherkin_call")) {
                    thereWasName = true;

                    assertThat(evaluatedToken).isPresent();
                    assertThat(evaluatedToken.get().getPosition())
                            .isEqualTo(new Position(token.getStartOffset(), token.getText().length()));
                    assertThat(evaluatedToken.get().getToken().getData()).isEqualTo("call_token");

                } else if (!token.getText().contains("var_asgn")) {
                    assertThat(evaluatedToken).isNotPresent();
                }
                previousTokens.add(token);
            }
        }
        assertThat(thereWasName).isTrue();
    }

    @Test
    public void executableCallIsRecognized_evenWhenPositionIsInsideToken() {
        boolean thereWasName = false;
        final List<RobotLine> lines = TokensSource.createTokensInLines();
        for (final RobotLine line : lines) {
            for (final IRobotLineElement token : line.getLineElements()) {
                final int positionInsideToken = new Random().nextInt(token.getText().length());
                final Optional<PositionedTextToken> evaluatedToken = evaluate(token, positionInsideToken, lines);

                if (token.getText().equals("call") || token.getText().equals(":FOR") || token.getText().equals("\\")
                        || token.getText().contains("gherkin_call")) {
                    thereWasName = true;

                    assertThat(evaluatedToken).isPresent();
                    assertThat(evaluatedToken.get().getPosition())
                            .isEqualTo(new Position(token.getStartOffset() + positionInsideToken,
                                    token.getText().length() - positionInsideToken));
                    assertThat(evaluatedToken.get().getToken().getData()).isEqualTo("call_token");

                } else if (!token.getText().contains("var_asgn")) {
                    assertThat(evaluatedToken).isNotPresent();
                }
            }
        }
        assertThat(thereWasName).isTrue();
    }

    @Test
    public void variableUsageIsRecognized() {
        boolean thereWasVar = false;
        final List<RobotLine> lines = TokensSource.createTokensInLines();
        for (final RobotLine line : lines) {
            for (final IRobotLineElement token : line.getLineElements()) {
                final Optional<PositionedTextToken> evaluatedToken = evaluate(token, lines);

                if (token.getText().contains("var_asgn")) {
                    thereWasVar = true;

                    assertThat(evaluatedToken).isPresent();
                    assertThat(evaluatedToken.get().getPosition())
                            .isEqualTo(new Position(token.getStartOffset(), token.getText().length()));
                    assertThat(evaluatedToken.get().getToken().getData()).isEqualTo("var_token");

                } else if (!token.getText().equals("call") && !token.getText().equals(":FOR")
                        && !token.getText().equals("\\") && !token.getText().contains("gherkin_call")) {
                    assertThat(evaluatedToken).isNotPresent();
                }
            }
        }
        assertThat(thereWasVar).isTrue();
    }

    @Test
    public void variableUsageIsRecognized_evenWhenPositionIsInsideToken() {
        boolean thereWasVar = false;
        final List<RobotLine> lines = TokensSource.createTokensInLines();
        for (final RobotLine line : lines) {
            for (final IRobotLineElement token : line.getLineElements()) {
                final int positionInsideToken = new Random().nextInt(token.getText().length());
                final Optional<PositionedTextToken> evaluatedToken = evaluate(token, positionInsideToken, lines);

                if (token.getText().contains("var_asgn")) {
                    thereWasVar = true;

                    assertThat(evaluatedToken).isPresent();
                    assertThat(evaluatedToken.get().getPosition())
                            .isEqualTo(new Position(token.getStartOffset() + positionInsideToken,
                                    token.getText().length() - positionInsideToken));
                    assertThat(evaluatedToken.get().getToken().getData()).isEqualTo("var_token");

                } else if (!token.getText().equals("call") && !token.getText().equals(":FOR")
                        && !token.getText().equals("\\") && !token.getText().contains("gherkin_call")) {
                    assertThat(evaluatedToken).isNotPresent();
                }
            }
        }
        assertThat(thereWasVar).isTrue();
    }

    @Test
    public void variableTokenIsDetected_whenPositionedInsideVariable() {
        final String var1 = "${var}";
        final String var2 = "@{list}";
        final String var3 = "&{dir}";

        final String content = "abc" + var1 + "def" + var2 + "[0]ghi" + var3 + "[key]jkl";

        final List<Position> varPositions = new ArrayList<>();
        varPositions.add(new Position(content.indexOf(var1), var1.length()));
        varPositions.add(new Position(content.indexOf(var2), var2.length()));
        varPositions.add(new Position(content.indexOf(var3), var3.length()));

        for (final RobotTokenType actionType : EnumSet.of(RobotTokenType.KEYWORD_ACTION_NAME,
                RobotTokenType.TEST_CASE_ACTION_NAME, RobotTokenType.KEYWORD_ACTION_ARGUMENT,
                RobotTokenType.TEST_CASE_ACTION_ARGUMENT)) {

            final RobotToken token = createToken(actionType, content);

            for (final Position position : varPositions) {
                for (int offset = 0; offset < position.getLength(); offset++) {
                    final RobotLine line = new RobotLine(0, null);
                    line.getLineElements().add(token);

                    final List<RobotLine> lines = newArrayList(line);
                    final Optional<PositionedTextToken> evaluatedToken = evaluate(token, position.getOffset() + offset,
                            lines);
                    assertThat(evaluatedToken).isPresent();
                    assertThat(evaluatedToken.get().getPosition()).isEqualTo(position);
                    assertThat(evaluatedToken.get().getToken().getData()).isEqualTo("var_token");
                }
            }
        }
    }

    @Test
    public void keywordCallTokenIsDetected_whenPositionedOutsideVariables() {
        final String text1 = "abc";
        final String text2 = "def";
        final String text3 = "ghi";
        final String text4 = "jkl";
        final String content = text1 + "${var}" + text2 + "@{list}[0]" + text3 + "&{dir}[key]" + text4;

        final List<Position> nonVarPositions = new ArrayList<>();
        nonVarPositions.add(new Position(content.indexOf(text1), text1.length()));
        nonVarPositions.add(new Position(content.indexOf(text2), text2.length()));
        nonVarPositions.add(new Position(content.indexOf(text3), text3.length()));
        nonVarPositions.add(new Position(content.indexOf(text4), text4.length()));

        for (final RobotTokenType actionType : EnumSet.of(RobotTokenType.KEYWORD_ACTION_NAME,
                RobotTokenType.TEST_CASE_ACTION_NAME, RobotTokenType.KEYWORD_ACTION_ARGUMENT,
                RobotTokenType.TEST_CASE_ACTION_ARGUMENT)) {

            final RobotToken token = createToken(actionType, content);

            for (final Position position : nonVarPositions) {
                for (int offset = 0; offset < position.getLength(); offset++) {
                    final RobotLine line = new RobotLine(0, null);
                    line.getLineElements().add(token);

                    final Optional<PositionedTextToken> evaluatedToken = evaluate(token, position.getOffset() + offset,
                            newArrayList(line));
                    assertThat(evaluatedToken).isPresent();
                    final Position expectedPosition = nonVarPositions.indexOf(position) == 0
                            ? new Position(position.getOffset() + offset, position.getLength() - offset)
                            : position;
                    assertThat(evaluatedToken.get().getPosition()).isEqualTo(expectedPosition);
                    assertThat(evaluatedToken.get().getToken().getData()).isEqualTo("call_token");
                }
            }
        }
    }

    private RobotToken createToken(final RobotTokenType actionType, final String kwCall) {
        final RobotToken token = RobotToken.create(kwCall, newArrayList(actionType, RobotTokenType.VARIABLE_USAGE));
        token.setLineNumber(1);
        token.setStartColumn(0);
        token.setStartOffset(0);
        return token;
    }

    private Optional<PositionedTextToken> evaluate(final IRobotLineElement token, final List<RobotLine> lines) {
        return evaluate(token, 0, lines);
    }

    private Optional<PositionedTextToken> evaluate(final IRobotLineElement token, final int position,
            final List<RobotLine> lines) {
        return Stream
                .of(ExecutableCallRule.forExecutableInTestCase(new Token("call_token"), new Token("var_token")),
                        ExecutableCallRule.forExecutableInKeyword(new Token("call_token"), new Token("var_token")))
                .filter(rule -> rule.isApplicable(token))
                .findFirst()
                .flatMap(rule -> rule.evaluate(token, position, lines));
    }
}
