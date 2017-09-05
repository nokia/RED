/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.execution.debug.contexts;

import java.net.URI;
import java.util.Optional;

import org.rf.ide.core.execution.debug.RobotLineBreakpoint;
import org.rf.ide.core.execution.debug.StackFrameContext;
import org.rf.ide.core.testdata.model.FileRegion;


abstract class DefaultContext implements StackFrameContext {

    @Override
    public boolean isErroneous() {
        return false;
    }

    @Override
    public Optional<String> getErrorMessage() {
        return Optional.empty();
    }

    @Override
    public Optional<URI> getAssociatedPath() {
        return Optional.empty();
    }

    @Override
    public Optional<FileRegion> getFileRegion() {
        return Optional.empty();
    }

    @Override
    public StackFrameContext previousContext() {
        return this;
    }

    @Override
    public Optional<RobotLineBreakpoint> getLineBreakpoint() {
        return Optional.empty();
    }
}
