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
import java.util.Set;
import java.util.function.Predicate;

import org.eclipse.jface.text.rules.IToken;
import org.rf.ide.core.testdata.model.table.exec.descs.VariableExtractor;
import org.rf.ide.core.testdata.model.table.exec.descs.ast.mapping.IElementDeclaration;
import org.rf.ide.core.testdata.model.table.exec.descs.ast.mapping.NonEnvironmentDeclarationMapper;
import org.rf.ide.core.testdata.model.table.keywords.names.GherkinStyleSupport;
import org.rf.ide.core.testdata.model.table.keywords.names.QualifiedKeywordName;
import org.rf.ide.core.testdata.text.read.IRobotLineElement;
import org.rf.ide.core.testdata.text.read.IRobotTokenType;
import org.rf.ide.core.testdata.text.read.RobotLine;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;
import org.rf.ide.core.validation.SpecialKeywords;

public class ExecutableCallRule extends VariableUsageRule {

    public static ExecutableCallRule forExecutableInTestCase(final IToken textToken, final IToken gherkinToken,
            final IToken libraryToken, final IToken quoteToken, final IToken embeddedVariablesToken) {
        return new ExecutableCallRule(textToken, gherkinToken, libraryToken, quoteToken, embeddedVariablesToken,
                EnumSet.of(RobotTokenType.TEST_CASE_ACTION_NAME, RobotTokenType.TEST_CASE_ACTION_ARGUMENT),
                elem -> elem.getTypes().contains(RobotTokenType.TEST_CASE_NAME));
    }

    public static ExecutableCallRule forExecutableInTask(final IToken textToken, final IToken gherkinToken,
            final IToken libraryToken, final IToken quoteToken, final IToken embeddedVariablesToken) {
        return new ExecutableCallRule(textToken, gherkinToken, libraryToken, quoteToken, embeddedVariablesToken,
                EnumSet.of(RobotTokenType.TASK_ACTION_NAME, RobotTokenType.TASK_ACTION_ARGUMENT),
                elem -> elem.getTypes().contains(RobotTokenType.TASK_NAME));
    }

    public static ExecutableCallRule forExecutableInKeyword(final IToken textToken, final IToken gherkinToken,
            final IToken libraryToken, final IToken quoteToken, final IToken embeddedVariablesToken) {
        return new ExecutableCallRule(textToken, gherkinToken, libraryToken, quoteToken, embeddedVariablesToken,
                EnumSet.of(RobotTokenType.KEYWORD_ACTION_NAME, RobotTokenType.KEYWORD_ACTION_ARGUMENT),
                elem -> elem.getTypes().contains(RobotTokenType.KEYWORD_NAME));
    }

    private final Set<? extends IRobotTokenType> acceptableTypes;

    private final IToken gherkinToken;

    private final IToken libraryToken;

    private final IToken quoteToken;

    private final Predicate<IRobotLineElement> shouldStopOnElement;

    protected ExecutableCallRule(final IToken textToken, final IToken gherkinToken, final IToken libraryToken,
            final IToken quoteToken, final IToken embeddedVariablesToken,
            final Set<? extends IRobotTokenType> acceptableTypes,
            final Predicate<IRobotLineElement> shouldStopOnElement) {
        super(embeddedVariablesToken, textToken);
        this.gherkinToken = gherkinToken;
        this.libraryToken = libraryToken;
        this.quoteToken = quoteToken;
        this.acceptableTypes = acceptableTypes;
        this.shouldStopOnElement = shouldStopOnElement;
    }

    @Override
    public boolean isApplicable(final IRobotLineElement token) {
        return super.isApplicable(token) && acceptableTypes.contains(token.getTypes().get(0))
                && !token.getTypes().contains(RobotTokenType.FOR_CONTINUE_TOKEN)
                && !token.getTypes().contains(RobotTokenType.FOR_WITH_END_CONTINUATION)
                && !token.getTypes().contains(RobotTokenType.FOR_TOKEN)
                && !token.getTypes().contains(RobotTokenType.FOR_END_TOKEN)
                && !token.getTypes().contains(RobotTokenType.TEST_CASE_TEMPLATE_ARGUMENT)
                && !token.getTypes().contains(RobotTokenType.TASK_TEMPLATE_ARGUMENT);
    }

    @Override
    public Optional<PositionedTextToken> evaluate(final IRobotLineElement token, final int offsetInToken,
            final List<RobotLine> context) {
        if (shouldBeColored(token, context, shouldStopOnElement)) {
            Optional<PositionedTextToken> evaluated = evaluateGherkin(token, offsetInToken);
            if (evaluated.isPresent()) {
                return evaluated;
            }

            evaluated = evaluateLibrary(token, offsetInToken);
            if (evaluated.isPresent()) {
                return evaluated;
            }

            evaluated = evaluateAssignment(token, offsetInToken);
            if (evaluated.isPresent()) {
                return evaluated;
            }

            evaluated = super.evaluate(token, offsetInToken, context);
            if (evaluated.isPresent()) {
                return evaluated;
            }

            return evaluateQuotes(token.getStartOffset(), offsetInToken, token.getText(), offsetInToken);
        }
        return Optional.empty();
    }

    private Optional<PositionedTextToken> evaluateGherkin(final IRobotLineElement token, final int offsetInToken) {
        if (offsetInToken == 0) {
            final String textAfterPrefix = GherkinStyleSupport.getTextAfterGherkinPrefixesIfExists(token.getText());
            final int prefixLength = token.getText().length() - textAfterPrefix.length();
            if (prefixLength > 0) {
                return Optional.of(new PositionedTextToken(gherkinToken, token.getStartOffset(), prefixLength));
            }
        }
        return Optional.empty();
    }

    private Optional<PositionedTextToken> evaluateLibrary(final IRobotLineElement token,
            final int offsetInToken) {
        final int dotIndex = token.getText().indexOf('.', offsetInToken);
        if (dotIndex > 0) {
            final int quoteOpeningIndex = token.getText().indexOf('"', offsetInToken);
            if (quoteOpeningIndex == -1 || dotIndex < quoteOpeningIndex) {
                final int varOpeningBracketIndex = token.getText().indexOf('{', offsetInToken);
                if (varOpeningBracketIndex == -1 || dotIndex < varOpeningBracketIndex) {
                    return Optional.of(new PositionedTextToken(libraryToken, token.getStartOffset() + offsetInToken,
                            dotIndex - offsetInToken + 1));
                }
            }
        }
        return Optional.empty();
    }

    private Optional<PositionedTextToken> evaluateAssignment(final IRobotLineElement token, final int offsetInToken) {
        if (token.getTypes().contains(RobotTokenType.ASSIGNMENT)) {
            final int assignIndex = token.getText().lastIndexOf('=');
            if (assignIndex >= offsetInToken && token.getText().substring(offsetInToken).trim().equals("=")) {
                return Optional.of(new PositionedTextToken(ISyntaxColouringRule.DEFAULT_TOKEN,
                        token.getStartOffset() + assignIndex, 1));
            }
        }
        return Optional.empty();
    }

    private Optional<PositionedTextToken> evaluateQuotes(final int tokenStartOffset, final int offsetInToken,
            final String textToEvaluate, final int fromIndex) {
        final int quoteOpenIndex = textToEvaluate.indexOf('"', fromIndex);
        if (quoteOpenIndex != -1) {
            if (fromIndex < quoteOpenIndex) {
                return Optional.of(new PositionedTextToken(nonVarToken, tokenStartOffset + offsetInToken,
                        quoteOpenIndex - fromIndex));
            }
            final int quoteCloseIndex = textToEvaluate.indexOf('"', quoteOpenIndex + 1);
            if (quoteOpenIndex < quoteCloseIndex) {
                return Optional.of(new PositionedTextToken(quoteToken,
                        tokenStartOffset + quoteOpenIndex + offsetInToken - fromIndex,
                        quoteCloseIndex - quoteOpenIndex + 1));
            }
        }
        return Optional.of(new PositionedTextToken(nonVarToken, tokenStartOffset + offsetInToken,
                textToEvaluate.length() - fromIndex));
    }

    @Override
    protected VariableExtractor createVariableExtractor() {
        return new VariableExtractor(new NonEnvironmentDeclarationMapper());
    }

    @Override
    protected Optional<PositionedTextToken> evaluateNonVariablePart(final IRobotLineElement token,
            final int offsetInToken, final IElementDeclaration declaration) {
        return evaluateQuotes(token.getStartOffset(), offsetInToken, declaration.getText(),
                offsetInToken - declaration.getStart().getStart());
    }

    protected boolean shouldBeColored(final IRobotLineElement token, final List<RobotLine> context,
            final Predicate<IRobotLineElement> shouldStopOnElement) {

        final List<RobotToken> tokensBefore = getPreviousTokensInExecutable(token, context, shouldStopOnElement);

        if (!isNestedKeyword(token, context, tokensBefore)) {
            for (final RobotToken prevToken : tokensBefore) {
                final List<IRobotTokenType> types = prevToken.getTypes();
                if (!prevToken.getText().isEmpty()
                        && !types.contains(RobotTokenType.VARIABLE_USAGE)
                        && !types.contains(RobotTokenType.ASSIGNMENT)
                        && !types.contains(RobotTokenType.PRETTY_ALIGN_SPACE)
                        && !types.contains(RobotTokenType.FOR_CONTINUE_TOKEN)
                        && !types.contains(RobotTokenType.FOR_CONTINUE_ARTIFICIAL_TOKEN)) {
                    return false;
                }
            }
        }
        return true;
    }

    protected final boolean isNestedKeyword(final IRobotLineElement token, final List<RobotLine> context,
            final List<RobotToken> tokensBefore) {
        for (int i = tokensBefore.size() - 1; i >= 0; i--) {
            final QualifiedKeywordName qualifiedKeywordName = QualifiedKeywordName
                    .fromOccurrence(tokensBefore.get(i).getText());
            if (SpecialKeywords.isNestingKeyword(qualifiedKeywordName)) {
                final List<RobotToken> execTokens = new ArrayList<>(tokensBefore);
                execTokens.add((RobotToken) token);
                execTokens.addAll(getNextTokensInExecutable(token, context));

                if (SpecialKeywords.isKeywordNestedInKeyword(qualifiedKeywordName, i, tokensBefore.size() - i,
                        execTokens)) {
                    return true;
                }
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
            if (isComment(robotLine)) {
                continue;
            }

            final int previousElemIndex = line - 1 == i ? robotLine.getLineElements().indexOf(token) - 1
                    : robotLine.getLineElements().size() - 1;

            for (int j = previousElemIndex; j >= 0; j--) {
                final IRobotLineElement element = robotLine.getLineElements().get(j);
                if (shouldBeAdded(element)) {
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

    private static boolean isComment(final RobotLine robotLine) {
        for (final IRobotLineElement element : robotLine.getLineElements()) {
            if (element instanceof RobotToken) {
                final List<IRobotTokenType> types = element.getTypes();
                return types.contains(RobotTokenType.START_HASH_COMMENT)
                        || types.contains(RobotTokenType.COMMENT_CONTINUE)
                        || types.contains(RobotTokenType.EMPTY_CELL)
                        || robotLine.isEmpty();
            }
        }
        return false;
    }

    private static boolean isContinuation(final RobotLine robotLine) {
        boolean isFirst = true;
        for (final IRobotLineElement element : robotLine.getLineElements()) {
            if (element instanceof RobotToken) {
                final List<IRobotTokenType> types = element.getTypes();
                if (isFirst && types.contains(RobotTokenType.FOR_CONTINUE_TOKEN)) {
                    isFirst = false;
                } else {
                    return types.contains(RobotTokenType.PREVIOUS_LINE_CONTINUE)
                            || types.contains(RobotTokenType.SETTING_NAME_DUPLICATION)
                            || types.contains(RobotTokenType.TEST_CASE_SETTING_NAME_DUPLICATION)
                            || types.contains(RobotTokenType.KEYWORD_SETTING_NAME_DUPLICATION);
                }
            }
        }
        return false;
    }

    protected List<RobotToken> getPreviousTokensInExecutable(final IRobotLineElement token, final List<RobotLine> lines,
            final Predicate<IRobotLineElement> shouldStopOnElement) {
        return getPreviousTokensInThisExecutable(token, lines, shouldStopOnElement);
    }

    protected List<RobotToken> getNextTokensInExecutable(final IRobotLineElement token,
            final List<RobotLine> lines) {
        final List<RobotToken> tokens = new ArrayList<>();
        final int line = token.getLineNumber();
        for (int i = line - 1; i < lines.size(); i++) {
            final RobotLine robotLine = lines.get(i);
            final int nextElemIndex = line - 1 == i ? robotLine.getLineElements().indexOf(token) + 1 : 0;

            for (int j = nextElemIndex; j < robotLine.getLineElements().size(); j++) {
                final IRobotLineElement element = robotLine.getLineElements().get(j);
                if (shouldBeAdded(element)) {
                    tokens.add(0, (RobotToken) element);
                }
            }
            if (i + 1 < lines.size() && !isContinuation(lines.get(i + 1))) {
                break;
            }
        }
        return tokens;
    }

    private static boolean shouldBeAdded(final IRobotLineElement element) {
        if (element instanceof RobotToken) {
            final List<IRobotTokenType> types = element.getTypes();
            return !types.contains(RobotTokenType.PREVIOUS_LINE_CONTINUE)
                    && !types.contains(RobotTokenType.PRETTY_ALIGN_SPACE)
                    && !types.contains(RobotTokenType.FOR_CONTINUE_TOKEN)
                    && !types.contains(RobotTokenType.START_HASH_COMMENT)
                    && !types.contains(RobotTokenType.COMMENT_CONTINUE)
                    && !types.contains(RobotTokenType.SETTING_NAME_DUPLICATION)
                    && !types.contains(RobotTokenType.TEST_CASE_SETTING_NAME_DUPLICATION)
                    && !types.contains(RobotTokenType.KEYWORD_SETTING_NAME_DUPLICATION);
        }
        return false;
    }
}
