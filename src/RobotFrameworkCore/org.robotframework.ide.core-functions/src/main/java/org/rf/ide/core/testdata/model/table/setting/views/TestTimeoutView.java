/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.table.setting.views;

import java.util.List;

import org.rf.ide.core.testdata.model.table.setting.TestTimeout;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;

public class TestTimeoutView extends TestTimeout {

    private static final long serialVersionUID = -4081479282712030528L;

    private final List<TestTimeout> timeouts;

    public TestTimeoutView(final List<TestTimeout> timeouts) {
        super(timeouts.get(0).getDeclaration());
        this.timeouts = timeouts;

        initialize();
    }

    private void initialize() {
        for (final TestTimeout timeout : timeouts) {
            if (timeout.getTimeout() != null) {
                if (getTimeout() != null) {
                    super.addMessageArgument(timeout.getTimeout());
                } else {
                    super.setTimeout(timeout.getTimeout());
                }
            }
            for (final RobotToken msg : timeout.getMessageArguments()) {
                super.addMessageArgument(msg);
            }
            for (final RobotToken comment : timeout.getComment()) {
                super.addCommentPart(comment);
            }
        }
    }

    @Override
    public void addMessageArgument(final String messageArgument) {
        joinIfNeeded();
        super.addMessageArgument(messageArgument);
    }

    @Override
    public void addMessageArgument(final RobotToken messageArgument) {
        joinIfNeeded();
        super.addMessageArgument(messageArgument);
    }

    @Override
    public void setMessageArgument(final int index, final String argument) {
        final List<RobotToken> tokens = super.getMessageArguments();
        if (tokens.size() <= index) {
            joinIfNeeded();
        }
        super.setMessageArgument(index, argument);
    }

    @Override
    public void setMessageArgument(final int index, final RobotToken argument) {
        final List<RobotToken> tokens = super.getMessageArguments();
        if (tokens.size() <= index) {
            joinIfNeeded();
        }
        super.setMessageArgument(index, argument);
    }

    @Override
    public void addCommentPart(final RobotToken rt) {
        joinIfNeeded();
        super.addCommentPart(rt);
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

    private synchronized void joinIfNeeded() {
        if (timeouts.size() > 1) {
            timeouts.clear();
            timeouts.add(this);
        }
    }
}
