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

    private final List<TestTimeout> timeouts;

    public TestTimeoutView(final List<TestTimeout> timeouts) {
        super(timeouts.get(0).getDeclaration());
        this.timeouts = timeouts;

        // join timeout for this view
        final TestTimeout timeout = new TestTimeout(getDeclaration());
        joinTimeout(timeout, timeouts);
        copyWithoutJoinIfNeededExecution(timeout);
    }

    private void copyWithoutJoinIfNeededExecution(final TestTimeout timeout) {
        super.setTimeout(timeout.getTimeout());

        for (final RobotToken token : timeout.getMessageArguments()) {
            super.addMessageArgument(token);
        }

        for (final RobotToken comment : timeout.getComment()) {
            super.addCommentPart(comment);
        }
    }

    @Override
    public void setTimeout(final RobotToken timeout) {
        joinIfNeeded();
        super.setTimeout(timeout);
    }

    @Override
    public void setTimeout(final String timeout) {
        joinIfNeeded();
        super.setTimeout(timeout);
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
        joinIfNeeded();
        super.setMessageArgument(index, argument);
    }

    @Override
    public void setMessageArgument(final int index, final RobotToken argument) {
        joinIfNeeded();
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
            TestTimeout joined = new TestTimeout(getDeclaration());
            joinTimeout(joined, timeouts);
            timeouts.clear();
            timeouts.add(joined);
        }
    }

    private void joinTimeout(final TestTimeout target, final List<TestTimeout> timeouts) {
        for (final TestTimeout time : timeouts) {
            if (time.getTimeout() != null) {
                if (target.getTimeout() != null) {
                    target.addMessageArgument(time.getTimeout());
                } else {
                    target.setTimeout(time.getTimeout());
                }
            }

            for (final RobotToken msg : time.getMessageArguments()) {
                target.addMessageArgument(msg);
            }

            for (final RobotToken comment : time.getComment()) {
                target.addCommentPart(comment);
            }
        }
    }
}
