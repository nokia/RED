/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.execution.debug;

import static com.google.common.collect.Sets.newHashSet;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Set;
import java.util.regex.Pattern;

import org.rf.ide.core.execution.debug.contexts.ExecutableCallContext;

public enum KeywordCallType {

    SETUP,
    TEARDOWN,
    NORMAL_CALL,
    FOR,
    FOR_ITERATION;

    private static final Set<String> SETUPS = newHashSet("suite setup", "test setup", "setup");

    private static final Set<String> TEARDOWNS = newHashSet("suite teardown", "test teardown", "teardown");

    private static final Set<String> FORS = newHashSet("suite for", "test for", "for");

    private static final Set<String> FOR_ITEMS = newHashSet("suite foritem", "test foritem", "for item");

    public static KeywordCallType from(final String keywordType) {
        if (keywordType.equalsIgnoreCase("keyword")) {
            return NORMAL_CALL;

        } else if (SETUPS.contains(keywordType.toLowerCase())) {
            return SETUP;

        } else if (TEARDOWNS.contains(keywordType.toLowerCase())) {
            return TEARDOWN;

        } else if (FORS.contains(keywordType.toLowerCase())) {
            return FOR;

        } else if (FOR_ITEMS.contains(keywordType.toLowerCase())) {
            return FOR_ITERATION;

        } else {
            throw new IllegalStateException("Unrecognized keyword type: " + keywordType);
        }
    }

    static class KeywordsTypesFixer {

        RunningKeyword keywordStarting(final RunningKeyword keyword,
                @SuppressWarnings("unused") final StackFrameContext context) {
            return keyword;
        }

        RunningKeyword keywordStarted(final RunningKeyword keyword) {
            return keyword;
        }

        void keywordEnded() {
            // nothing to do
        }
    }

    /**
     * RobotFramework in versions older than 3.0 was wrongly reporting types
     * of keywords. This fixer is able to fix those types to proper ones as used
     * by RF 3.0 or later.
     * For more informations regarding the issue see the discussion
     * at https://github.com/robotframework/robotframework/issues/2248
     * 
     * @author anglart
     */
    static class KeywordsTypesForRf29Fixer extends KeywordsTypesFixer {

        private static final Pattern FOR_LOOP_NAME = Pattern
                .compile("^\\$\\{.+\\} (in|in range|in zip|in enumerate) \\[.+\\]");

        private final Deque<RunningKeyword> fixedKeywords = new ArrayDeque<>();

        @Override
        RunningKeyword keywordStarting(final RunningKeyword keyword, final StackFrameContext context) {
            final RunningKeyword fixedKeyword = fix(keyword, context);
            fixedKeywords.push(fixedKeyword);
            return fixedKeyword;
        }

        @Override
        RunningKeyword keywordStarted(final RunningKeyword keyword) {
            return fixedKeywords.peek();
        }

        @Override
        void keywordEnded() {
            fixedKeywords.pop();
        }

        private RunningKeyword fix(final RunningKeyword keyword, final StackFrameContext context) {
            if (fixedKeywords.isEmpty()) {
                return keyword;
            }

            switch (fixedKeywords.peek().getType()) {
                case FOR:
                    // only iterations are possible under FOR-type keyword
                    return new RunningKeyword(keyword, KeywordCallType.FOR_ITERATION);
                case FOR_ITERATION:
                    // only normal calls are possible under FOR_ITERATION-type keyword
                    return new RunningKeyword(keyword, KeywordCallType.NORMAL_CALL);
                default:
                    return new RunningKeyword(keyword, getFixedKeywordType(keyword, context));
            }
        }

        private KeywordCallType getFixedKeywordType(final RunningKeyword keyword, final StackFrameContext context) {
            // usually there cannot be nested SETUPS or TEARDOWNS, but with a little exception that
            // keyword teardown can happen
            if (keyword.isTeardown() && context instanceof ExecutableCallContext) {
                final ExecutableCallContext executableCallContext = (ExecutableCallContext) context;
                if (executableCallContext.isOnLastExecutable()) {
                    return KeywordCallType.TEARDOWN;
                }
            }

            return looksLikeForLoop(keyword) ? KeywordCallType.FOR : KeywordCallType.NORMAL_CALL;
        }

        private boolean looksLikeForLoop(final RunningKeyword keyword) {
            return FOR_LOOP_NAME.matcher(keyword.getName().toLowerCase()).matches();
        }
    }
}
