/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.source.colouring;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import org.eclipse.jface.text.rules.IToken;
import org.rf.ide.core.testdata.model.table.keywords.names.QualifiedKeywordName;
import org.rf.ide.core.testdata.text.read.IRobotLineElement;
import org.rf.ide.core.testdata.text.read.IRobotTokenType;
import org.rf.ide.core.testdata.text.read.RobotLine;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;
import org.rf.ide.core.validation.SpecialKeywords;

public class ExecutableCallRule extends VariableUsageRule {

    public static ExecutableCallRule forExecutableInTestCase(final IToken textToken,
            final IToken embeddedVariablesToken) {
        return new ExecutableCallRule(textToken, embeddedVariablesToken,
                EnumSet.of(RobotTokenType.TEST_CASE_ACTION_NAME, RobotTokenType.TEST_CASE_ACTION_ARGUMENT),
                elem -> elem.getTypes().contains(RobotTokenType.TEST_CASE_NAME));
    }

    public static ExecutableCallRule forExecutableInKeyword(final IToken textToken,
            final IToken embeddedVariablesToken) {
        return new ExecutableCallRule(textToken, embeddedVariablesToken,
                EnumSet.of(RobotTokenType.KEYWORD_ACTION_NAME, RobotTokenType.KEYWORD_ACTION_ARGUMENT),
                elem -> elem.getTypes().contains(RobotTokenType.KEYWORD_NAME));
    }

    private final EnumSet<RobotTokenType> acceptableTypes;

    private final IToken textToken;

    private final Predicate<IRobotLineElement> shouldStopOnElement;

    protected ExecutableCallRule(final IToken textToken, final IToken embeddedVariablesToken,
            final EnumSet<RobotTokenType> acceptableTypes, final Predicate<IRobotLineElement> shouldStopOnElement) {
        super(embeddedVariablesToken);
        this.textToken = textToken;
        this.acceptableTypes = acceptableTypes;
        this.shouldStopOnElement = shouldStopOnElement;
    }

    @Override
    public boolean isApplicable(final IRobotLineElement token) {
        return super.isApplicable(token) && acceptableTypes.contains(token.getTypes().get(0));
    }

    @Override
    protected IToken getTokenForNonVariablePart() {
        return textToken;
    }

    @Override
    public Optional<PositionedTextToken> evaluate(final IRobotLineElement token, final int offsetInToken,
            final List<RobotLine> context) {
        if (shouldBeColored(token, context, shouldStopOnElement)) {
            final Optional<PositionedTextToken> evaluated = super.evaluate(token, offsetInToken, context);
            if (evaluated.isPresent()) {
                return evaluated;
            }
            return Optional.of(new PositionedTextToken(textToken, token.getStartOffset() + offsetInToken,
                    token.getText().length() - offsetInToken));
        }
        return Optional.empty();
    }

    protected boolean shouldBeColored(final IRobotLineElement token, final List<RobotLine> context,
            final Predicate<IRobotLineElement> shouldStopOnElement) {

        final List<RobotToken> tokensBefore = getPreviousTokensInThisExecutable(token, context, shouldStopOnElement);

        if (!isNestedKeyword(token, context, tokensBefore)) {
            for (final RobotToken prevToken : tokensBefore) {
                final List<IRobotTokenType> types = prevToken.getTypes();
                if (!prevToken.getText().isEmpty()
                        && !types.contains(RobotTokenType.VARIABLE_USAGE)
                        && !types.contains(RobotTokenType.ASSIGNMENT)
                        && !types.contains(RobotTokenType.PRETTY_ALIGN_SPACE)
                        && !types.contains(RobotTokenType.FOR_CONTINUE_TOKEN)) {
                    return false;
                }
            }
        }
        return true;
    }

    protected final boolean isNestedKeyword(final IRobotLineElement token, final List<RobotLine> context,
            final List<RobotToken> tokensBefore) {
        for (int j = tokensBefore.size() - 1; j >= 0; j--) {
            final QualifiedKeywordName qualifiedKeywordName = QualifiedKeywordName
                    .fromOccurrence(tokensBefore.get(j).getText());
            if (SpecialKeywords.isNestingKeyword(qualifiedKeywordName)) {
                final List<RobotToken> execTokens = new ArrayList<>(tokensBefore);
                execTokens.add((RobotToken) token);
                execTokens.addAll(getNextTokensInThisExecutable(token, context));

                return SpecialKeywords.isKeywordNestedInKeyword(qualifiedKeywordName, j, tokensBefore.size() - j,
                        execTokens);
            }
        }
        return false;
    }

    static List<RobotToken> getPreviousTokensInThisExecutable(final IRobotLineElement token,
            final List<RobotLine> lines, final Predicate<IRobotLineElement> shouldStopOnElement) {
        final List<RobotToken> tokens = new ArrayList<>();

        final int line = token.getLineNumber();
        for (int i = line - 1; i >= 0; i--) {
            final RobotLine robotLine = lines.get(i);
            final int previousElemIndex = line - 1 == i ? robotLine.getLineElements().indexOf(token) - 1
                    : robotLine.getLineElements().size() - 1;

            for (int j = previousElemIndex; j >= 0; j--) {
                final IRobotLineElement element = robotLine.getLineElements().get(j);
                if (element instanceof RobotToken && !isContinuation(element)) {
                    if (shouldStopOnElement.test(element)) {
                        return tokens;
                    }
                    tokens.add(0, (RobotToken) element);
                }
            }
            if (!isContinuation(robotLine)) {
                break;
            }
        }
        return tokens;
    }

    private static List<RobotToken> getNextTokensInThisExecutable(final IRobotLineElement token,
            final List<RobotLine> lines) {
        final List<RobotToken> tokens = new ArrayList<>();
        final int line = token.getLineNumber();
        for (int i = line - 1; i < lines.size(); i++) {
            final RobotLine robotLine = lines.get(i);
            final int nextElemIndex = line - 1 == i ? robotLine.getLineElements().indexOf(token) + 1 : 0;

            for (int j = nextElemIndex; j < robotLine.getLineElements().size(); j++) {
                final IRobotLineElement element = robotLine.getLineElements().get(j);
                if (element instanceof RobotToken && !isContinuation(element)) {
                    tokens.add(0, (RobotToken) element);
                }
            }
            if (i + 1 < lines.size() && !isContinuation(lines.get(i + 1))) {
                break;
            }
        }
        return tokens;
    }

    private static boolean isContinuation(final RobotLine robotLine) {
        for (final IRobotLineElement element : robotLine.getLineElements()) {
            if (element instanceof RobotToken) {
                return isContinuation(element);
            }
        }
        return false;
    }

    private static boolean isContinuation(final IRobotLineElement element) {
        return element.getTypes().contains(RobotTokenType.PREVIOUS_LINE_CONTINUE);
    }
}
