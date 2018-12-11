/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.assist;

import java.util.List;

import com.google.common.collect.ImmutableList;

class RedWithNameProposal extends BaseAssistProposal {

    private final static List<String> ARGUMENTS = ImmutableList.of("alias");

    RedWithNameProposal(final String word, final ProposalMatch match) {
        super(word, match);
    }

    @Override
    public List<String> getArguments() {
        return ARGUMENTS;
    }
}
