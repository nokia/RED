/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.mapping;

import org.rf.ide.core.testdata.model.RobotFileOutput;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

/**
 * @author wypych
 */
public class FileOutputsMatchUpdater {

    public void update(final RobotFileOutput oldModifiedOutput, final RobotFileOutput alreadyDumpedContent) {
        validateBasicThatOutputFromSameFile(oldModifiedOutput, alreadyDumpedContent);

        final ListMultimap<RobotTokenType, RobotToken> oldViewAboutTokens = extractRobotTokens(oldModifiedOutput);
        final ListMultimap<RobotTokenType, RobotToken> newViewAboutTokens = extractRobotTokens(alreadyDumpedContent);

        validateThatTheSameTokensInView(oldViewAboutTokens, newViewAboutTokens);
        // now map normal tokens
        // then just trashes
        // last update lines
        // we replacing new by old in places in already dumped content
        // next we remove old lines and we putting new Robot Lines
    }

    @VisibleForTesting
    protected void validateBasicThatOutputFromSameFile(final RobotFileOutput oldModifiedOutput,
            final RobotFileOutput alreadyDumpedContent) {
        // check if file are the same name and location
    }

    @VisibleForTesting
    protected ListMultimap<RobotTokenType, RobotToken> extractRobotTokens(final RobotFileOutput tokensHolder) {
        final ListMultimap<RobotTokenType, RobotToken> tokensPerType = ArrayListMultimap.create();

        return tokensPerType;
    }

    private void validateThatTheSameTokensInView(final ListMultimap<RobotTokenType, RobotToken> oldViewAboutTokens,
            final ListMultimap<RobotTokenType, RobotToken> newViewAboutTokens) {
        // check the same types are in both maps
        // next check if each type contains the same number of tokens with the same content
    }
}
