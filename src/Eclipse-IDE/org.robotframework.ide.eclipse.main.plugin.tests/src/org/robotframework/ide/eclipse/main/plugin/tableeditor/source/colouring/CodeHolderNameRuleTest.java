/*
 * Copyright 2020 Nokia Solutions and Networks
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
import java.util.function.Function;
import java.util.function.Predicate;

import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.rules.Token;
import org.junit.jupiter.api.Test;
import org.rf.ide.core.environment.RobotVersion;
import org.rf.ide.core.testdata.text.read.IRobotLineElement;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;
import org.rf.ide.core.testdata.text.read.separators.Separator;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.colouring.ISyntaxColouringRule.PositionedTextToken;

public class CodeHolderNameRuleTest {

    @Test
    public void ruleIsApplicableOnlyForRobotTokens() {
        final CodeHolderNameRule rule = CodeHolderNameRule.forKeyword(new Token("name_token"), new Token("var_token"),
                () -> new RobotVersion(3, 1));

        assertThat(rule.isApplicable(new RobotToken())).isTrue();
        assertThat(rule.isApplicable(new Separator())).isFalse();
        assertThat(rule.isApplicable(mock(IRobotLineElement.class))).isFalse();
    }

    @Test
    public void simpleKeywordNameIsRecognized() {
        final CodeHolderNameRule rule = CodeHolderNameRule.forKeyword(new Token("name_token"), new Token("var_token"),
                () -> new RobotVersion(3, 1));
        assertSimpleNameIsRecognized(rule, TokensSource.createTokens(), text -> text.contains("userkw"));
    }

    @Test
    public void simpleKeywordNameIsRecognized_evenWhenPositionIsInsideToken() {
        final CodeHolderNameRule rule = CodeHolderNameRule.forKeyword(new Token("name_token"), new Token("var_token"),
                () -> new RobotVersion(3, 1));
        assertSimpleNameIsRecognizedWhenPositionedInside(rule, TokensSource.createTokens(),
                text -> text.contains("userkw"));
    }

    @Test
    public void simpleTestCaseNameIsRecognized() {
        final CodeHolderNameRule rule = CodeHolderNameRule.forTest(new Token("name_token"), new Token("var_token"),
                () -> new RobotVersion(3, 1));
        assertSimpleNameIsRecognized(rule, TokensSource.createTokens(),
                text -> text.equals("case 1") || text.equals("case 2"));
    }

    @Test
    public void simpleTestCaseNameIsRecognized_evenWhenPositionIsInsideToken() {
        final CodeHolderNameRule rule = CodeHolderNameRule.forTest(new Token("name_token"), new Token("var_token"),
                () -> new RobotVersion(3, 1));
        assertSimpleNameIsRecognizedWhenPositionedInside(rule, TokensSource.createTokens(),
                text -> text.equals("case 1") || text.equals("case 2"));
    }

    @Test
    public void simpleTaskNameIsRecognized() {
        final CodeHolderNameRule rule = CodeHolderNameRule.forTask(new Token("name_token"), new Token("var_token"),
                () -> new RobotVersion(3, 1));
        assertSimpleNameIsRecognized(rule, TokensSource.createRpaTokens(),
                text -> text.equals("task 1") || text.equals("task 2"));
    }

    @Test
    public void simpleTaskNameIsRecognized_evenWhenPositionIsInsideToken() {
        final CodeHolderNameRule rule = CodeHolderNameRule.forTask(new Token("name_token"), new Token("var_token"),
                () -> new RobotVersion(3, 1));
        assertSimpleNameIsRecognizedWhenPositionedInside(rule, TokensSource.createRpaTokens(),
                text -> text.equals("task 1") || text.equals("task 2"));
    }

    @Test
    public void variableTokenIsDetected_whenPositionedInsideVariable() {
        final String var1 = "${var}";
        final String var2 = "@{list}[0]";
        final String var3 = "&{dir}[key]";

        final String content = "abc" + var1 + "def" + var2 + "ghi" + var3 + "jkl";

        final List<Position> varPositions = new ArrayList<>();
        varPositions.add(new Position(content.indexOf(var1), var1.length()));
        varPositions.add(new Position(content.indexOf(var2), var2.length()));
        varPositions.add(new Position(content.indexOf(var3), var3.length()));

        final CodeHolderNameRule rule = CodeHolderNameRule.forKeyword(new Token("name_token"), new Token("var_token"),
                () -> new RobotVersion(3, 1));
        final RobotToken token = createKeywordNameToken(content);

        for (final Position position : varPositions) {
            for (int offset = 0; offset < position.getLength(); offset++) {
                final Optional<PositionedTextToken> evaluatedToken = rule.evaluate(token, position.getOffset() + offset,
                        new ArrayList<>());
                assertThat(evaluatedToken).isPresent();
                assertThat(evaluatedToken.get().getPosition())
                        .isEqualTo(new Position(position.getOffset() + offset, position.getLength() - offset));
                assertThat(evaluatedToken.get().getToken().getData()).isEqualTo("var_token");
            }
        }
    }

    @Test
    public void keywordNameTokenIsDetected_whenPositionedOutsideVariables() {
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

        final RobotToken token = createKeywordNameToken(content);

        final CodeHolderNameRule rule = CodeHolderNameRule.forKeyword(new Token("name_token"), new Token("var_token"),
                () -> new RobotVersion(3, 1));
        for (final Position position : nonVarPositions) {
            for (int offset = 0; offset < position.getLength(); offset++) {
                final Optional<PositionedTextToken> evaluatedToken = rule.evaluate(token, position.getOffset() + offset,
                        new ArrayList<>());
                assertThat(evaluatedToken).isPresent();
                assertThat(evaluatedToken.get().getPosition())
                        .isEqualTo(new Position(position.getOffset() + offset, position.getLength() - offset));
                assertThat(evaluatedToken.get().getToken().getData()).isEqualTo("name_token");
            }
        }
    }

    private static void assertSimpleNameIsRecognized(final CodeHolderNameRule rule, final List<RobotToken> sourceTokens,
            final Predicate<String> recognizedTokenContentCondition) {
        assertNameIsRecognizedWithGivenPositionInside(rule, sourceTokens, text -> 0, recognizedTokenContentCondition);
    }

    private static void assertSimpleNameIsRecognizedWhenPositionedInside(final CodeHolderNameRule rule,
            final List<RobotToken> sourceTokens, final Predicate<String> recognizedTokenContentCondition) {

        assertNameIsRecognizedWithGivenPositionInside(rule, sourceTokens, text -> new Random().nextInt(text.length()),
                recognizedTokenContentCondition);
    }

    private static void assertNameIsRecognizedWithGivenPositionInside(final CodeHolderNameRule rule,
            final List<RobotToken> sourceTokens, final Function<String, Integer> insidePositionSupplier,
            final Predicate<String> recognizedTokenContentCondition) {
        boolean thereWasName = false;
        for (final RobotToken token : sourceTokens) {
            final int positionInsideToken = insidePositionSupplier.apply(token.getText());
            final Optional<PositionedTextToken> evaluatedToken = rule.evaluate(token, positionInsideToken,
                    new ArrayList<>());

            if (recognizedTokenContentCondition.test(token.getText())) {
                thereWasName = true;

                assertThat(evaluatedToken).isPresent();
                assertThat(evaluatedToken.get().getPosition())
                        .isEqualTo(new Position(token.getStartOffset(), token.getText().length()));
                assertThat(evaluatedToken.get().getToken().getData()).isEqualTo("name_token");

            } else {
                assertThat(evaluatedToken).isNotPresent();
            }
        }
        assertThat(thereWasName).isTrue();
    }

    private RobotToken createKeywordNameToken(final String kwName) {
        return createNameToken(kwName, RobotTokenType.KEYWORD_NAME);
    }

    private RobotToken createNameToken(final String kwName, final RobotTokenType mainType) {
        final RobotToken token = RobotToken.create(kwName, newArrayList(mainType, RobotTokenType.VARIABLE_USAGE));
        token.setLineNumber(1);
        token.setStartColumn(0);
        token.setStartOffset(0);
        return token;
    }
}
