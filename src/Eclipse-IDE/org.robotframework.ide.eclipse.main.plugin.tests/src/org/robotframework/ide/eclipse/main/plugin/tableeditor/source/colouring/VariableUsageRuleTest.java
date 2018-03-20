/*
* Copyright 2016 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.robotframework.ide.eclipse.main.plugin.tableeditor.source.colouring;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.rules.Token;
import org.junit.Test;
import org.rf.ide.core.testdata.text.read.IRobotLineElement;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;
import org.rf.ide.core.testdata.text.read.separators.Separator;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.colouring.ISyntaxColouringRule.PositionedTextToken;

public class VariableUsageRuleTest {

    private final VariableUsageRule testedRule = new VariableUsageRule(new Token("token"));

    @Test
    public void ruleIsApplicableOnlyForRobotTokens() {
        assertThat(testedRule.isApplicable(new RobotToken())).isTrue();
        assertThat(testedRule.isApplicable(new Separator())).isFalse();
        assertThat(testedRule.isApplicable(mock(IRobotLineElement.class))).isFalse();
    }

    @Test
    public void nothingIsDetected_whenThereIsNoVariableUsage() {
        final RobotToken token = RobotToken.create("${variable}");

        final Optional<PositionedTextToken> evaluatedToken = evaluate(token);
        assertThat(evaluatedToken).isNotPresent();
    }

    @Test
    public void variableUsageIsDetected_forSimpleVariable() {
        final RobotToken token = createToken("${variable}");

        final Optional<PositionedTextToken> evaluatedToken = evaluate(token);

        assertThat(evaluatedToken).isPresent();
        assertThat(evaluatedToken.get().getPosition())
                .isEqualTo(new Position(token.getStartOffset(), token.getText().length()));
        assertThat(evaluatedToken.get().getToken().getData()).isEqualTo("token");
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

        final RobotToken token = createToken(content);

        for (final Position position : varPositions) {
            for (int offset = 0; offset < position.getLength(); offset++) {
                final Optional<PositionedTextToken> evaluatedToken = evaluate(token, position.getOffset() + offset);
                assertThat(evaluatedToken).isPresent();
                assertThat(evaluatedToken.get().getPosition()).isEqualTo(position);
                assertThat(evaluatedToken.get().getToken().getData()).isEqualTo("token");
            }
        }
    }

    @Test
    public void defaultTokenIsDetected_whenPositionedOutsideVariables() {
        final String text1 = "abc";
        final String text2 = "def";
        final String index1 = "[0]";
        final String text3 = "ghi";
        final String index2 = "[key]";
        final String text4 = "jkl";
        final String content = text1 + "${var}" + text2 + "@{list}" + index1 + text3 + "&{dir}" + index2 + text4;

        final List<Position> nonVarPositions = new ArrayList<>();
        nonVarPositions.add(new Position(content.indexOf(text1), text1.length()));
        nonVarPositions.add(new Position(content.indexOf(text2), text2.length()));
        nonVarPositions.add(new Position(content.indexOf(index1), index1.length()));
        nonVarPositions.add(new Position(content.indexOf(text3), text3.length()));
        nonVarPositions.add(new Position(content.indexOf(index2), index2.length()));
        nonVarPositions.add(new Position(content.indexOf(text4), text4.length()));

        final RobotToken token = createToken(content);

        for (final Position position : nonVarPositions) {
            for (int offset = 0; offset < position.getLength(); offset++) {
                final Optional<PositionedTextToken> evaluatedToken = evaluate(token, position.getOffset() + offset);
                assertThat(evaluatedToken).isPresent();
                final Position expectedPosition = nonVarPositions.indexOf(position) == 0
                        ? new Position(position.getOffset() + offset, position.getLength() - offset)
                        : position;
                assertThat(evaluatedToken.get().getPosition()).isEqualTo(expectedPosition);
                assertThat(evaluatedToken.get().getToken()).isSameAs(ISyntaxColouringRule.DEFAULT_TOKEN);
            }
        }
    }

    private RobotToken createToken(final String content) {
        final RobotToken token = RobotToken.create(content, EnumSet.of(RobotTokenType.VARIABLE_USAGE));
        token.setLineNumber(1);
        token.setStartColumn(0);
        token.setStartOffset(0);
        return token;
    }

    private Optional<PositionedTextToken> evaluate(final RobotToken token) {
        return evaluate(token, 0);
    }

    private Optional<PositionedTextToken> evaluate(final RobotToken token, final int position) {
        return testedRule.evaluate(token, position, new ArrayList<>());
    }
}
