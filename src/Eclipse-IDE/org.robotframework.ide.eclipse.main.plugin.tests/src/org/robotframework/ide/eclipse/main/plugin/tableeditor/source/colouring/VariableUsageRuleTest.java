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
import java.util.Collection;
import java.util.EnumSet;
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

        final Optional<PositionedTextToken> evaluatedToken = testedRule.evaluate(token, 0,
                new ArrayList<IRobotLineElement>());
        assertThat(evaluatedToken).isNotPresent();
    }

    @Test
    public void variableUsageIsDetected_forSimpleVariable() {
        final RobotToken token = createToken("${variable}");

        final Optional<PositionedTextToken> evaluatedToken = testedRule.evaluate(token, 0, new ArrayList<IRobotLineElement>());

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

        final String content = "abc" + var1 + "def" + var2 + "ghi" + var3 + "[0]jkl";

        final Collection<Position> varPositions = newArrayList();
        varPositions.add(new Position(content.indexOf(var1), var1.length()));
        varPositions.add(new Position(content.indexOf(var2), var2.length()));
        varPositions.add(new Position(content.indexOf(var3), var3.length()));

        final RobotToken token = createToken(content);

        for (final Position position : varPositions) {
            for (int i = position.getOffset(); i < position.getLength(); i++) {
                final Optional<PositionedTextToken> evaluatedToken = testedRule.evaluate(token, i,
                        new ArrayList<IRobotLineElement>());
                assertThat(evaluatedToken).isPresent();
                assertThat(evaluatedToken.get().getPosition()).isEqualTo(position);
                assertThat(evaluatedToken.get().getToken().getData()).isEqualTo("token");
            }
        }
    }

    @Test
    public void defaultTokenIsDetected_whenPositionedOutsideVariables() {
        final String def1 = "abc";
        final String def2 = "def";
        final String def3 = "ghi";
        final String def4 = "[0]jkl";
        final String content = def1 + "${var}" + def2 + "@{list}" + def3 + "&{dir}" + def4;

        final Collection<Position> varPositions = newArrayList();
        varPositions.add(new Position(content.indexOf(def1), def1.length()));
        varPositions.add(new Position(content.indexOf(def2), def2.length()));
        varPositions.add(new Position(content.indexOf(def3), def3.length()));
        varPositions.add(new Position(content.indexOf(def4), def4.length()));

        final RobotToken token = createToken(content);

        for (final Position position : varPositions) {
            for (int i = position.getOffset(); i < position.getLength(); i++) {
                final Optional<PositionedTextToken> evaluatedToken = testedRule.evaluate(token, i,
                        new ArrayList<IRobotLineElement>());
                assertThat(evaluatedToken).isPresent();
                assertThat(evaluatedToken.get().getPosition()).isEqualTo(position);
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

}
