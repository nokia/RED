/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.source.colouring;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Optional;
import java.util.Random;
import java.util.TreeMap;
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

    private final ExecutableCallRule tcTestedRule = ExecutableCallRule.forExecutableInTestCase(new Token("call_token"),
            new Token("gherkin_token"), new Token("lib_token"), new Token("quote_token"), new Token("var_token"));

    private final ExecutableCallRule taskTestedRule = ExecutableCallRule.forExecutableInTask(new Token("call_token"),
            new Token("gherkin_token"), new Token("lib_token"), new Token("quote_token"), new Token("var_token"));

    private final ExecutableCallRule kwTestedRule = ExecutableCallRule.forExecutableInKeyword(new Token("call_token"),
            new Token("gherkin_token"), new Token("lib_token"), new Token("quote_token"), new Token("var_token"));

    @Test
    public void ruleIsApplicableOnlyForRobotTokens() {
        for (final RobotTokenType actionType : EnumSet.of(RobotTokenType.TEST_CASE_ACTION_NAME,
                RobotTokenType.TEST_CASE_ACTION_ARGUMENT)) {
            assertThat(tcTestedRule.isApplicable(RobotToken.create("", newArrayList(actionType)))).isTrue();
            assertThat(taskTestedRule.isApplicable(RobotToken.create("", newArrayList(actionType)))).isFalse();
            assertThat(kwTestedRule.isApplicable(RobotToken.create("", newArrayList(actionType)))).isFalse();
        }
        for (final RobotTokenType actionType : EnumSet.of(RobotTokenType.TASK_ACTION_NAME,
                RobotTokenType.TASK_ACTION_ARGUMENT)) {
            assertThat(tcTestedRule.isApplicable(RobotToken.create("", newArrayList(actionType)))).isFalse();
            assertThat(taskTestedRule.isApplicable(RobotToken.create("", newArrayList(actionType)))).isTrue();
            assertThat(kwTestedRule.isApplicable(RobotToken.create("", newArrayList(actionType)))).isFalse();
        }
        for (final RobotTokenType actionType : EnumSet.of(RobotTokenType.KEYWORD_ACTION_NAME,
                RobotTokenType.KEYWORD_ACTION_ARGUMENT)) {
            assertThat(tcTestedRule.isApplicable(RobotToken.create("", newArrayList(actionType)))).isFalse();
            assertThat(taskTestedRule.isApplicable(RobotToken.create("", newArrayList(actionType)))).isFalse();
            assertThat(kwTestedRule.isApplicable(RobotToken.create("", newArrayList(actionType)))).isTrue();
        }
        for (final ExecutableCallRule rule : newHashSet(tcTestedRule, taskTestedRule, kwTestedRule)) {
            assertThat(rule.isApplicable(new RobotToken())).isFalse();
            assertThat(rule.isApplicable(new Separator())).isFalse();
            assertThat(rule.isApplicable(mock(IRobotLineElement.class))).isFalse();
        }
    }

    @Test
    public void executableCallIsRecognized() {
        final List<IRobotLineElement> previousTokens = new ArrayList<>();

        boolean thereWasName = false;
        final List<RobotLine> lines = TokensSource.createTokensInLines();
        for (final RobotLine line : lines) {
            for (final IRobotLineElement token : line.getLineElements()) {
                final Optional<PositionedTextToken> evaluatedToken = evaluate(token, lines);

                if (token.getText().equals("call")) {
                    thereWasName = true;

                    assertThat(evaluatedToken).isPresent();
                    assertThat(evaluatedToken.get().getPosition())
                            .isEqualTo(new Position(token.getStartOffset(), token.getText().length()));
                    assertThat(evaluatedToken.get().getToken().getData()).isEqualTo("call_token");

                } else if (!token.getText().contains("var_asgn") && !token.getText().contains("gherkin_call")) {
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

                if (token.getText().equals("call")) {
                    thereWasName = true;

                    assertThat(evaluatedToken).isPresent();
                    assertThat(evaluatedToken.get().getPosition())
                            .isEqualTo(new Position(token.getStartOffset() + positionInsideToken,
                                    token.getText().length() - positionInsideToken));
                    assertThat(evaluatedToken.get().getToken().getData()).isEqualTo("call_token");

                } else if (!token.getText().contains("var_asgn") && !token.getText().contains("gherkin_call")) {
                    assertThat(evaluatedToken).isNotPresent();
                }
            }
        }
        assertThat(thereWasName).isTrue();
    }

    @Test
    public void gherkinPrefixIsRecognized() {
        final List<IRobotLineElement> previousTokens = new ArrayList<>();

        boolean thereWasName = false;
        final List<RobotLine> lines = TokensSource.createTokensInLines();
        for (final RobotLine line : lines) {
            for (final IRobotLineElement token : line.getLineElements()) {
                final Optional<PositionedTextToken> evaluatedToken = evaluate(token, lines);

                if (token.getText().contains("gherkin_call")) {
                    thereWasName = true;

                    assertThat(evaluatedToken).isPresent();
                    assertThat(evaluatedToken.get().getPosition()).isEqualTo(
                            new Position(token.getStartOffset(), token.getText().length() - "gherkin_call".length()));
                    assertThat(evaluatedToken.get().getToken().getData()).isEqualTo("gherkin_token");
                } else if (!token.getText().contains("var_asgn") && !token.getText().equals("call")
                        && !token.getText().equals(":FOR")) {
                    assertThat(evaluatedToken).isNotPresent();
                }
                previousTokens.add(token);
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
    public void nestedKeywordsAreRecognized() {
        boolean thereAreNestedKw = false;
        final List<RobotLine> lines = TokensSource.createTokensInLinesWithSpecialNestingKeywords();
        for (final RobotLine line : lines) {
            for (final IRobotLineElement token : line.getLineElements()) {
                final int positionInsideToken = new Random().nextInt(token.getText().length());
                final Optional<PositionedTextToken> evaluatedToken = evaluate(token, positionInsideToken, lines);

                if (token.getText().equals("nestedkw")) {
                    thereAreNestedKw = true;

                    assertThat(evaluatedToken).isPresent();
                    assertThat(evaluatedToken.get().getPosition())
                            .isEqualTo(new Position(token.getStartOffset() + positionInsideToken,
                                    token.getText().length() - positionInsideToken));
                    assertThat(evaluatedToken.get().getToken().getData()).isEqualTo("call_token");

                } else if (!token.getText().equals("Run Keyword If") && !token.getText().equals("Run Keywords")
                        && !token.getText().equals(":FOR") && !token.getText().equals("\\")
                        && !token.getText().contains("var_asgn")) {
                    assertThat(evaluatedToken).isNotPresent();
                }
            }
        }
        assertThat(thereAreNestedKw).isTrue();
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
                RobotTokenType.TEST_CASE_ACTION_NAME, RobotTokenType.TASK_ACTION_NAME,
                RobotTokenType.KEYWORD_ACTION_ARGUMENT, RobotTokenType.TEST_CASE_ACTION_ARGUMENT,
                RobotTokenType.TASK_ACTION_ARGUMENT)) {

            final RobotToken token = createToken(actionType, content);
            final List<RobotLine> lines = newArrayList(line(token));

            for (final Position position : varPositions) {
                for (int offset = 0; offset < position.getLength(); offset++) {
                    final Optional<PositionedTextToken> evaluatedToken = evaluate(token, position.getOffset() + offset,
                            lines);
                    assertThat(evaluatedToken).isPresent();
                    assertThat(evaluatedToken.get().getPosition())
                            .isEqualTo(new Position(position.getOffset() + offset, position.getLength() - offset));
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
                RobotTokenType.TEST_CASE_ACTION_NAME, RobotTokenType.TASK_ACTION_NAME,
                RobotTokenType.KEYWORD_ACTION_ARGUMENT, RobotTokenType.TEST_CASE_ACTION_ARGUMENT,
                RobotTokenType.TASK_ACTION_ARGUMENT)) {

            final RobotToken token = createToken(actionType, content);
            final List<RobotLine> lines = newArrayList(line(token));

            for (final Position position : nonVarPositions) {
                for (int offset = 0; offset < position.getLength(); offset++) {
                    final Optional<PositionedTextToken> evaluatedToken = evaluate(token, position.getOffset() + offset,
                            lines);
                    assertThat(evaluatedToken).isPresent();
                    assertThat(evaluatedToken.get().getPosition())
                            .isEqualTo(new Position(position.getOffset() + offset, position.getLength() - offset));
                    assertThat(evaluatedToken.get().getToken().getData()).isEqualTo("call_token");
                }
            }
        }
    }

    @Test
    public void keywordCallQuoteTokenIsDetected() {
        final String content = "abc\"x\"def\"xy\"ghi\"xyz\"jkl";

        for (final RobotTokenType actionType : EnumSet.of(RobotTokenType.KEYWORD_ACTION_NAME,
                RobotTokenType.TEST_CASE_ACTION_NAME, RobotTokenType.TASK_ACTION_NAME,
                RobotTokenType.KEYWORD_ACTION_ARGUMENT, RobotTokenType.TEST_CASE_ACTION_ARGUMENT,
                RobotTokenType.TASK_ACTION_ARGUMENT)) {

            final RobotToken token = createToken(actionType, content);
            final List<RobotLine> lines = newArrayList(line(token));

            for (int i = 0; i < content.length(); i++) {
                final Optional<PositionedTextToken> evaluatedToken = evaluate(token, i, lines);
                assertThat(evaluatedToken).isPresent();

                final int nextQuoteIndex = content.indexOf('"', i + 1);
                if (content.charAt(i) == '"' && nextQuoteIndex >= 0) {
                    final int length = nextQuoteIndex - i + 1;
                    assertThat(evaluatedToken.get().getPosition()).isEqualTo(new Position(i, length));
                    assertThat(evaluatedToken.get().getToken().getData()).isEqualTo("quote_token");
                } else {
                    final int length = nextQuoteIndex != -1 ? nextQuoteIndex - i : content.length() - i;
                    assertThat(evaluatedToken.get().getPosition()).isEqualTo(new Position(i, length));
                    assertThat(evaluatedToken.get().getToken().getData()).isEqualTo("call_token");
                }
            }
        }
    }

    @Test
    public void keywordLibraryPrefixTokenIsDetected() {
        final String content = "SomeLibrary.With.Dots.KeywordCall With \"arg.1\" And ${v.A.r} Embedded";

        final List<Position> libraryPositions = new ArrayList<>();
        libraryPositions.add(new Position(0, 12));
        libraryPositions.add(new Position(12, 5));
        libraryPositions.add(new Position(17, 5));

        final List<Position> keywordPositions = new ArrayList<>();
        keywordPositions.add(new Position(22, 17));
        keywordPositions.add(new Position(46, 5));
        keywordPositions.add(new Position(59, 9));

        for (final RobotTokenType actionType : EnumSet.of(RobotTokenType.KEYWORD_ACTION_NAME,
                RobotTokenType.TEST_CASE_ACTION_NAME, RobotTokenType.TASK_ACTION_NAME,
                RobotTokenType.KEYWORD_ACTION_ARGUMENT, RobotTokenType.TEST_CASE_ACTION_ARGUMENT,
                RobotTokenType.TASK_ACTION_ARGUMENT)) {

            final RobotToken token = createToken(actionType, content);
            final List<RobotLine> lines = newArrayList(line(token));

            for (int i = 0; i < content.length(); i++) {
                final Optional<PositionedTextToken> evaluatedToken = evaluate(token, i, lines);
                assertThat(evaluatedToken).isPresent();

                for (final Position position : libraryPositions) {
                    if (position.includes(i)) {
                        assertThat(evaluatedToken.get().getPosition())
                                .isEqualTo(new Position(i, position.getOffset() + position.getLength() - i));
                        assertThat(evaluatedToken.get().getToken().getData()).isEqualTo("lib_token");
                    }
                }

                for (final Position position : keywordPositions) {
                    if (position.includes(i)) {
                        assertThat(evaluatedToken.get().getPosition())
                                .isEqualTo(new Position(i, position.getOffset() + position.getLength() - i));
                        assertThat(evaluatedToken.get().getToken().getData()).isEqualTo("call_token");
                    }
                }
            }
        }
    }

    @Test
    public void allTokensAreDetected() {
        final String content = "Given SomeLib.Embedded \"Name\" Kw @{variable}[0] With \"Argument\" With Quotes And ${var}";

        final NavigableMap<Integer, String> tokenPositions = new TreeMap<>();
        tokenPositions.put(0, "gherkin_token");
        tokenPositions.put(6, "lib_token");
        tokenPositions.put(14, "call_token");
        tokenPositions.put(23, "quote_token");
        tokenPositions.put(29, "call_token");
        tokenPositions.put(33, "var_token");
        tokenPositions.put(44, "var_token");
        tokenPositions.put(47, "call_token");
        tokenPositions.put(53, "quote_token");
        tokenPositions.put(63, "call_token");
        tokenPositions.put(80, "var_token");

        for (final RobotTokenType actionType : EnumSet.of(RobotTokenType.KEYWORD_ACTION_NAME,
                RobotTokenType.TEST_CASE_ACTION_NAME, RobotTokenType.TASK_ACTION_NAME,
                RobotTokenType.KEYWORD_ACTION_ARGUMENT, RobotTokenType.TEST_CASE_ACTION_ARGUMENT,
                RobotTokenType.TASK_ACTION_ARGUMENT)) {

            final RobotToken token = createToken(actionType, content);
            final List<RobotLine> lines = newArrayList(line(token));

            for (final Map.Entry<Integer, String> entry : tokenPositions.entrySet()) {
                final int position = entry.getKey();
                final int length = tokenPositions.higherEntry(position) == null ? content.length() - position
                        : tokenPositions.higherEntry(position).getKey() - position;
                final String tokenData = entry.getValue();

                final Optional<PositionedTextToken> evaluatedToken = evaluate(token, position, lines);
                assertThat(evaluatedToken).isPresent();
                assertThat(evaluatedToken.get().getPosition()).isEqualTo(new Position(position, length));
                assertThat(evaluatedToken.get().getToken().getData()).isEqualTo(tokenData);
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

    private RobotLine line(final RobotToken token) {
        final RobotLine line = new RobotLine(0, null);
        line.addLineElement(token);
        return line;
    }

    private Optional<PositionedTextToken> evaluate(final IRobotLineElement token, final List<RobotLine> lines) {
        return evaluate(token, 0, lines);
    }

    private Optional<PositionedTextToken> evaluate(final IRobotLineElement token, final int position,
            final List<RobotLine> lines) {
        return Stream.of(tcTestedRule, taskTestedRule, kwTestedRule)
                .filter(rule -> rule.isApplicable(token))
                .findFirst()
                .flatMap(rule -> rule.evaluate(token, position, lines));
    }
}
