/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.mapping.collect;

import java.util.Arrays;
import java.util.List;

import org.rf.ide.core.testdata.model.RobotFileOutput;
import org.rf.ide.core.testdata.text.read.IRobotTokenType;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

/**
 * @author wypych
 */
public class RobotTokensCollector {

    private static final List<ITableTokensCollector> tokenCollectors = Arrays.asList(new SettingsTokenCollector(),
            new VariablesTokenCollector(), new KeywordsTokenCollector(), new TestCasesTokenCollector());

    public ListMultimap<RobotTokenType, RobotToken> extractRobotTokens(final RobotFileOutput tokensHolder) {
        final ListMultimap<RobotTokenType, RobotToken> tokensPerType = ArrayListMultimap.create();

        for (final ITableTokensCollector collector : tokenCollectors) {
            final List<RobotToken> tokens = collector.collect(tokensHolder);
            update(tokens, tokensPerType);
            tokens.clear();
        }

        return tokensPerType;
    }

    @VisibleForTesting
    protected void update(final List<RobotToken> tokens, final ListMultimap<RobotTokenType, RobotToken> tokensPerType) {
        for (final RobotToken t : tokens) {
            final List<IRobotTokenType> types = t.getTypes();
            final RobotTokenType type;
            if (!types.isEmpty()) {
                type = (RobotTokenType) types.get(0);
            } else {
                type = RobotTokenType.UNKNOWN;
            }

            tokensPerType.put(type, t);
        }
    }
}
