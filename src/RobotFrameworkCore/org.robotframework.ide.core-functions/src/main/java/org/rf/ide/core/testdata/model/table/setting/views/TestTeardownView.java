/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.table.setting.views;

import java.util.List;

import org.rf.ide.core.testdata.model.AKeywordBaseSetting;
import org.rf.ide.core.testdata.model.table.setting.TestTeardown;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;

public class TestTeardownView extends TestTeardown {

    private static final long serialVersionUID = 2216473610245038551L;

    private final List<TestTeardown> teardowns;

    public TestTeardownView(final List<TestTeardown> teardowns) {
        super(teardowns.get(0).getDeclaration());
        this.teardowns = teardowns;

        initialize();
    }

    private void initialize() {
        for (final AKeywordBaseSetting<?> teardown : teardowns) {
            if (teardown.getKeywordName() != null) {
                if (getKeywordName() != null) {
                    super.addArgument(teardown.getKeywordName());
                } else {
                    super.setKeywordName(teardown.getKeywordName());
                }
            }
            for (final RobotToken arg : teardown.getArguments()) {
                super.addArgument(arg);
            }
            for (final RobotToken commentText : teardown.getComment()) {
                super.addCommentPart(commentText);
            }
        }
    }

    @Override
    public void addArgument(final String argument) {
        joinIfNeeded();
        super.addArgument(argument);
    }

    @Override
    public void addArgument(final RobotToken argument) {
        joinIfNeeded();
        super.addArgument(argument);
    }

    @Override
    public void setArgument(final int index, final String argument) {
        final List<RobotToken> tokens = super.getArguments();
        if (tokens.size() <= index) {
            joinIfNeeded();
        }
        super.setArgument(index, argument);
    }

    @Override
    public void setArgument(final int index, final RobotToken argument) {
        final List<RobotToken> tokens = super.getArguments();
        if (tokens.size() <= index) {
            joinIfNeeded();
        }
        super.setArgument(index, argument);
    }

    @Override
    public void setComment(final String comment) {
        joinIfNeeded();
        super.setComment(comment);
    }

    @Override
    public void setComment(final RobotToken rt) {
        joinIfNeeded();
        super.setComment(rt);
    }

    @Override
    public void addCommentPart(final RobotToken rt) {
        joinIfNeeded();
        super.addCommentPart(rt);
    }

    private synchronized void joinIfNeeded() {
        if (teardowns.size() > 1) {
            teardowns.clear();
            teardowns.add(this);
        }
    }
}
