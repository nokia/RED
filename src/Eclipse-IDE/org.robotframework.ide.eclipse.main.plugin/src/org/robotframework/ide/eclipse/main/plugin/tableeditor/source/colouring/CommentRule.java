/*
* Copyright 2016 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.robotframework.ide.eclipse.main.plugin.tableeditor.source.colouring;

import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jface.text.rules.IToken;
import org.rf.ide.core.testdata.text.read.IRobotLineElement;
import org.rf.ide.core.testdata.text.read.IRobotTokenType;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

public class CommentRule implements ISyntaxColouringRule {
    
    private final IToken textToken;

    private final ITodoTaskToken tasksToken;

    public CommentRule(final IToken textToken, final ITodoTaskToken tasksToken) {
        this.textToken = textToken;
        this.tasksToken = tasksToken;
    }

    @Override
    public boolean isApplicable(final IRobotLineElement token) {
        return token instanceof RobotToken;
    }

    @Override
    public Optional<PositionedTextToken> evaluate(final IRobotLineElement token, final int offsetInToken,
            final List<IRobotLineElement> analyzedTokens) {

        if (isComment(token) && tasksToken.isTaskDetectionEnabled()) {
            final Matcher tasksMatcher = tasksToken.getTasksPattern().matcher(token.getText());
            if (tasksMatcher.find(offsetInToken)) {
                final int start = tasksMatcher.start();
                final int end = tasksMatcher.end();

                if (start == offsetInToken) {
                    return Optional
                            .of(new PositionedTextToken(tasksToken, token.getStartOffset() + start, end - start));
                } else {
                    return Optional.of(new PositionedTextToken(textToken, token.getStartOffset() + offsetInToken,
                            start - offsetInToken));
                }
            }
            return Optional.of(new PositionedTextToken(textToken, token.getStartOffset() + offsetInToken,
                    token.getText().length() - offsetInToken));

        } else if (isComment(token) && !tasksToken.isTaskDetectionEnabled()) {
            return Optional.of(new PositionedTextToken(textToken, token.getStartOffset(), token.getText().length()));

        } else {
            return Optional.empty();
        }
    }

    private static boolean isComment(final IRobotLineElement token) {
        final List<IRobotTokenType> types = token.getTypes();
        return types.contains(RobotTokenType.START_HASH_COMMENT) || types.contains(RobotTokenType.COMMENT_CONTINUE);
    }

    public static interface ITodoTaskToken extends IToken {

        boolean isTaskDetectionEnabled();

        Pattern getTasksPattern();
    }
}
