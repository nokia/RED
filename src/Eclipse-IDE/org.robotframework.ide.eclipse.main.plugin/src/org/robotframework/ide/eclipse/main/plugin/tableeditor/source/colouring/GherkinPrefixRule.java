/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.source.colouring;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

import org.eclipse.jface.text.rules.IToken;
import org.rf.ide.core.testdata.model.table.keywords.names.GherkinStyleSupport;
import org.rf.ide.core.testdata.text.read.IRobotLineElement;
import org.rf.ide.core.testdata.text.read.RobotLine;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

public class GherkinPrefixRule implements ISyntaxColouringRule {

    public static GherkinPrefixRule forExecutableInTestCase(final IToken textToken) {
        return new GherkinPrefixRule(textToken,
                EnumSet.of(RobotTokenType.TEST_CASE_ACTION_NAME, RobotTokenType.TEST_CASE_ACTION_ARGUMENT),
                ExecutableCallRule.forExecutableInTestCase(null, null));
    }

    public static GherkinPrefixRule forExecutableInKeyword(final IToken textToken) {
        return new GherkinPrefixRule(textToken,
                EnumSet.of(RobotTokenType.KEYWORD_ACTION_NAME, RobotTokenType.KEYWORD_ACTION_ARGUMENT),
                ExecutableCallRule.forExecutableInTestCase(null, null));
    }

    private final IToken textToken;

    private final EnumSet<RobotTokenType> applicableTypes;

    private final ExecutableCallRule executableCallRule;

    private GherkinPrefixRule(final IToken textToken, final EnumSet<RobotTokenType> applicableTypes,
            final ExecutableCallRule executableCallRule) {
        this.textToken = textToken;
        this.applicableTypes = applicableTypes;
        this.executableCallRule = executableCallRule;
    }

    @Override
    public boolean isApplicable(final IRobotLineElement token) {
        return token instanceof RobotToken && applicableTypes.contains(token.getTypes().get(0));
    }

    @Override
    public Optional<PositionedTextToken> evaluate(final IRobotLineElement token, final int offsetInToken,
            final List<RobotLine> context) {
        
        final String textAfterPrefix = GherkinStyleSupport.getTextAfterGherkinPrefixesIfExists(token.getText());
        final int prefixLength = token.getText().length() - textAfterPrefix.length();
        if (prefixLength > 0 && shouldBeColored(token, offsetInToken, context)) {
            return Optional.of(new PositionedTextToken(textToken, token.getStartOffset(), prefixLength));
        }
        return Optional.empty();
    }

    private boolean shouldBeColored(final IRobotLineElement token, final int offsetInToken,
            final List<RobotLine> context) {
        return offsetInToken == 0 && executableCallRule.evaluate(token, offsetInToken, context).isPresent();
    }
}
