/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.views.debugshell;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.function.Predicate;

import org.eclipse.jface.text.rules.IToken;
import org.rf.ide.core.testdata.text.read.IRobotLineElement;
import org.rf.ide.core.testdata.text.read.RobotLine;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.colouring.ExecutableCallRule;


public class ExecutableCallInShellRule extends ExecutableCallRule {

    protected ExecutableCallInShellRule(final IToken textToken, final IToken libraryToken, final IToken quoteToken,
            final IToken embeddedVariablesToken) {
        super(textToken, textToken, libraryToken, quoteToken, embeddedVariablesToken,
                EnumSet.of(ShellTokenType.CALL_KW, ShellTokenType.CALL_ARG),
                elem -> elem.getTypes().contains(ShellTokenType.MODE_FLAG));
    }

    @Override
    protected boolean shouldBeColored(final IRobotLineElement token, final List<RobotLine> context,
            final Predicate<IRobotLineElement> shouldStopOnElement) {

        final List<RobotToken> tokensBefore = getPreviousTokensInExecutable(token, context, shouldStopOnElement);
        return token.getTypes().contains(ShellTokenType.CALL_KW) || isNestedKeyword(token, context, tokensBefore);
    }

    @Override
    protected List<RobotToken> getPreviousTokensInExecutable(final IRobotLineElement token, final List<RobotLine> lines,
            final Predicate<IRobotLineElement> shouldStopOnElement) {
        final List<RobotToken> tokens = new ArrayList<>();

        final int line = token.getLineNumber();
        for (int i = line - 1; i >= 0; i--) {
            final RobotLine robotLine = lines.get(i);

            final int previousElemIndex = line - 1 == i ? robotLine.getLineElements().indexOf(token) - 1
                    : robotLine.getLineElements().size() - 1;

            for (int j = previousElemIndex; j >= 0; j--) {
                final IRobotLineElement element = robotLine.getLineElements().get(j);
                if (element instanceof RobotToken && !element.getTypes().contains(ShellTokenType.MODE_CONTINUATION)) {
                    if (shouldStopOnElement.test(element)) {
                        return tokens;
                    }
                    tokens.add(0, (RobotToken) element);
                }
            }
            if (!robotLine.getLineElements().get(0).getTypes().contains(ShellTokenType.MODE_CONTINUATION)) {
                break;
            }
        }
        return tokens;
    }

    @Override
    protected List<RobotToken> getNextTokensInExecutable(final IRobotLineElement token, final List<RobotLine> lines) {
        final List<RobotToken> tokens = new ArrayList<>();
        final int line = token.getLineNumber();
        for (int i = line - 1; i < lines.size(); i++) {
            final RobotLine robotLine = lines.get(i);
            final int nextElemIndex = line - 1 == i ? robotLine.getLineElements().indexOf(token) + 1 : 0;

            for (int j = nextElemIndex; j < robotLine.getLineElements().size(); j++) {
                final IRobotLineElement element = robotLine.getLineElements().get(j);
                if (element instanceof RobotToken && !element.getTypes().contains(ShellTokenType.MODE_CONTINUATION)) {
                    tokens.add(0, (RobotToken) element);
                }
            }
            if (i + 1 < lines.size()
                    && !robotLine.getLineElements().get(0).getTypes().contains(ShellTokenType.MODE_CONTINUATION)) {
                break;
            }
        }
        return tokens;
    }
}
