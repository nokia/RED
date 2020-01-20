/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.source.colouring;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Optional;

import org.eclipse.jface.text.rules.IToken;
import org.junit.jupiter.api.Test;
import org.rf.ide.core.testdata.model.FilePosition;
import org.rf.ide.core.testdata.text.read.IRobotLineElement;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.KeywordUsagesFinder;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.colouring.ISyntaxColouringRule.PositionedTextToken;

public class KeywordCallOverridingRuleTest {

    @Test
    public void ruleIsApplicableIfAndOnlyIfWrappedRuleIsApplicable_1() {
        final ISyntaxColouringRule wrappedRule = mock(ISyntaxColouringRule.class);
        when(wrappedRule.isApplicable(any(IRobotLineElement.class))).thenReturn(true);

        final KeywordCallOverridingRule rule = new KeywordCallOverridingRule(wrappedRule, null, null, null);

        assertThat(rule.isApplicable(RobotToken.create(null))).isTrue();
        assertThat(rule.isApplicable(RobotToken.create(""))).isTrue();
        assertThat(rule.isApplicable(RobotToken.create("token"))).isTrue();
    }

    @Test
    public void ruleIsApplicableIfAndOnlyIfWrappedRuleIsApplicable_2() {
        final ISyntaxColouringRule wrappedRule = mock(ISyntaxColouringRule.class);
        when(wrappedRule.isApplicable(any(IRobotLineElement.class))).thenReturn(false);

        final KeywordCallOverridingRule rule = new KeywordCallOverridingRule(wrappedRule, null, null, null);

        assertThat(rule.isApplicable(RobotToken.create(null))).isFalse();
        assertThat(rule.isApplicable(RobotToken.create(""))).isFalse();
        assertThat(rule.isApplicable(RobotToken.create("token"))).isFalse();
    }

    @Test
    public void overridingTokenIsReturned_whenGivenTokenIsALibraryKeyword() {
        final KeywordUsagesFinder kwUsagesFinder = mock(KeywordUsagesFinder.class);
        when(kwUsagesFinder.isLibraryKeyword(42)).thenReturn(true);

        final RobotToken token = RobotToken.create("keyword", new FilePosition(1, 0, 42));

        final IToken overriden = mock(IToken.class);
        final IToken overridding = mock(IToken.class);

        final ISyntaxColouringRule wrappedRule = mock(ISyntaxColouringRule.class);
        when(wrappedRule.evaluate(token, 0, new ArrayList<>()))
                .thenReturn(Optional.of(new PositionedTextToken(overriden, 42, 7)));

        final KeywordCallOverridingRule rule = new KeywordCallOverridingRule(wrappedRule, overriden, overridding,
                kwUsagesFinder);

        final Optional<PositionedTextToken> result = rule.evaluate(token, 0, new ArrayList<>());
        assertThat(result).isPresent();
        assertThat(result.get().getToken()).isSameAs(overridding);
        assertThat(result.get().getOffset()).isEqualTo(42);
        assertThat(result.get().getLength()).isEqualTo(7);
    }

    @Test
    public void originalTokenIsReturned_whenGivenTokenIsNotALibraryKeyword() {
        final KeywordUsagesFinder kwUsagesFinder = mock(KeywordUsagesFinder.class);
        when(kwUsagesFinder.isLibraryKeyword(42)).thenReturn(false);

        final RobotToken token = RobotToken.create("keyword", new FilePosition(1, 0, 42));

        final IToken overriden = mock(IToken.class);
        final IToken overridding = mock(IToken.class);

        final ISyntaxColouringRule wrappedRule = mock(ISyntaxColouringRule.class);
        when(wrappedRule.evaluate(token, 0, new ArrayList<>()))
                .thenReturn(Optional.of(new PositionedTextToken(overriden, 42, 7)));

        final KeywordCallOverridingRule rule = new KeywordCallOverridingRule(wrappedRule, overriden, overridding,
                kwUsagesFinder);

        final Optional<PositionedTextToken> result = rule.evaluate(token, 0, new ArrayList<>());
        assertThat(result).isPresent();
        assertThat(result.get().getToken()).isSameAs(overriden);
        assertThat(result.get().getOffset()).isEqualTo(42);
        assertThat(result.get().getLength()).isEqualTo(7);
    }

    @Test
    public void originalTokenIsReturned_whenGivenTokenIsALibraryKeywordButSomeOtherTokenWasProvided() {
        final KeywordUsagesFinder kwUsagesFinder = mock(KeywordUsagesFinder.class);
        when(kwUsagesFinder.isLibraryKeyword(42)).thenReturn(true);

        final RobotToken token = RobotToken.create("keyword", new FilePosition(1, 0, 42));
        final IToken returned = mock(IToken.class);
        final IToken overriden = mock(IToken.class);
        final IToken overridding = mock(IToken.class);

        final ISyntaxColouringRule wrappedRule = mock(ISyntaxColouringRule.class);
        when(wrappedRule.evaluate(token, 0, new ArrayList<>()))
                .thenReturn(Optional.of(new PositionedTextToken(returned, 42, 7)));

        final KeywordCallOverridingRule rule = new KeywordCallOverridingRule(wrappedRule, overriden, overridding,
                kwUsagesFinder);

        final Optional<PositionedTextToken> result = rule.evaluate(token, 0, new ArrayList<>());
        assertThat(result).isPresent();
        assertThat(result.get().getToken()).isSameAs(returned);
        assertThat(result.get().getOffset()).isEqualTo(42);
        assertThat(result.get().getLength()).isEqualTo(7);
    }

    @Test
    public void originalTokenIsReturned_whenEmptyOptionalIsEvaluated() {
        final KeywordUsagesFinder kwUsagesFinder = mock(KeywordUsagesFinder.class);
        when(kwUsagesFinder.isLibraryKeyword(42)).thenReturn(true);

        final RobotToken token = RobotToken.create("keyword", new FilePosition(1, 0, 42));
        final IToken overriden = mock(IToken.class);
        final IToken overridding = mock(IToken.class);

        final ISyntaxColouringRule wrappedRule = mock(ISyntaxColouringRule.class);
        when(wrappedRule.evaluate(token, 0, new ArrayList<>())).thenReturn(Optional.empty());

        final KeywordCallOverridingRule rule = new KeywordCallOverridingRule(wrappedRule, overriden, overridding,
                kwUsagesFinder);

        final Optional<PositionedTextToken> result = rule.evaluate(token, 0, new ArrayList<>());
        assertThat(result).isEmpty();
    }
}
