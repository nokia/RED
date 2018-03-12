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
import java.util.List;
import java.util.Optional;
import java.util.Random;

import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.rules.Token;
import org.junit.Test;
import org.rf.ide.core.testdata.text.read.IRobotLineElement;
import org.rf.ide.core.testdata.text.read.IRobotTokenType;
import org.rf.ide.core.testdata.text.read.VersionAvailabilityInfo;
import org.rf.ide.core.testdata.text.read.VersionAvailabilityInfo.VersionAvailabilityInfoBuilder;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.separators.Separator;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.colouring.ISyntaxColouringRule.PositionedTextToken;

public class TokenTypeBasedRuleTest {

    private final IRobotTokenType recognizedType1 = new RecognizedTokenTypeMock1();
    private final IRobotTokenType recognizedType2 = new RecognizedTokenTypeMock2();
    private final IRobotTokenType unrecognizedType = new UnrecognizedTokenTypeMock();

    private final TokenTypeBasedRule testedRule = new TokenTypeBasedRule(new Token("token"),
            newArrayList(recognizedType1, recognizedType2));

    @Test
    public void ruleIsApplicableOnlyForRobotTokens() {
        assertThat(testedRule.isApplicable(new RobotToken())).isTrue();
        assertThat(testedRule.isApplicable(new Separator())).isFalse();
        assertThat(testedRule.isApplicable(mock(IRobotLineElement.class))).isFalse();
    }

    @Test
    public void tokenIsRecognized_whenItHasExpectedTypeAtFirstPosition_1() {
        final RobotToken token = RobotToken.create("text", newArrayList(recognizedType1, recognizedType2));
        token.setStartOffset(42);

        final Optional<PositionedTextToken> evaluatedToken = testedRule.evaluate(token, 0, new ArrayList<IRobotLineElement>());
        assertThat(evaluatedToken).isPresent();
        assertThat(evaluatedToken.get().getToken().getData()).isEqualTo("token");
        assertThat(evaluatedToken.get().getPosition())
                .isEqualTo(new Position(token.getStartOffset(), token.getText().length()));
    }

    @Test
    public void tokenIsRecognized_whenItHasExpectedTypeAtFirstPosition_2() {
        final RobotToken token = RobotToken.create("text", newArrayList(recognizedType2, recognizedType1));
        token.setStartOffset(42);

        final Optional<PositionedTextToken> evaluatedToken = testedRule.evaluate(token, 0,
                new ArrayList<IRobotLineElement>());
        assertThat(evaluatedToken).isPresent();
        assertThat(evaluatedToken.get().getToken().getData()).isEqualTo("token");
        assertThat(evaluatedToken.get().getPosition())
                .isEqualTo(new Position(token.getStartOffset(), token.getText().length()));
    }

    @Test
    public void tokenIsRecognized_whenItHasExpectedTypeAtFirstPositionRegardlessGivenOffsetInside() {
        final RobotToken token = RobotToken.create("text", newArrayList(recognizedType2, recognizedType1));
        token.setStartOffset(42);

        final int offsetInside = new Random().nextInt(token.getText().length());
        final Optional<PositionedTextToken> evaluatedToken = testedRule.evaluate(token, offsetInside,
                new ArrayList<IRobotLineElement>());
        assertThat(evaluatedToken).isPresent();
        assertThat(evaluatedToken.get().getToken().getData()).isEqualTo("token");
        assertThat(evaluatedToken.get().getPosition())
                .isEqualTo(new Position(token.getStartOffset(), token.getText().length()));
    }

    @Test
    public void tokenIsNotRecognized_whenItHasUnrecognizedType() {
        final RobotToken token = RobotToken.create("text", newArrayList(unrecognizedType));
        token.setStartOffset(42);

        final Optional<PositionedTextToken> evaluatedToken = testedRule.evaluate(token, 0,
                new ArrayList<IRobotLineElement>());
        assertThat(evaluatedToken).isNotPresent();
    }

    @Test
    public void tokenIsNotRecognized_evenIfItHasRecognizedTypeButNotOnFirstPosition() {
        final RobotToken token = RobotToken.create("text",
                newArrayList(unrecognizedType, recognizedType1, recognizedType2));
        token.setStartOffset(42);

        final Optional<PositionedTextToken> evaluatedToken = testedRule.evaluate(token, 0,
                new ArrayList<IRobotLineElement>());
        assertThat(evaluatedToken).isNotPresent();
    }

    @Test
    public void tokenIsNotRecognized_whenItHasUnrecognizedTypeRegardlessGivenOffsetInside() {
        final RobotToken token = RobotToken.create("text", newArrayList(unrecognizedType));
        token.setStartOffset(42);

        final int offsetInside = new Random().nextInt(token.getText().length());
        final Optional<PositionedTextToken> evaluatedToken = testedRule.evaluate(token, offsetInside,
                new ArrayList<IRobotLineElement>());
        assertThat(evaluatedToken).isNotPresent();
    }

    private class RecognizedTokenTypeMock1 implements IRobotTokenType {

        @Override
        public List<String> getRepresentation() {
            return new ArrayList<>();
        }

        @Override
        public List<VersionAvailabilityInfo> getVersionAvailabilityInfos() {
            return newArrayList(VersionAvailabilityInfoBuilder.create().build());
        }

        @Override
        public VersionAvailabilityInfo findVersionAvailablilityInfo(final String text) {
            return VersionAvailabilityInfoBuilder.create().build();
        }
    }

    private class RecognizedTokenTypeMock2 extends RecognizedTokenTypeMock1 {
        // nothing to reimplement
    }

    private class UnrecognizedTokenTypeMock extends RecognizedTokenTypeMock1 {
        // nothing to reimplement
    }
}
