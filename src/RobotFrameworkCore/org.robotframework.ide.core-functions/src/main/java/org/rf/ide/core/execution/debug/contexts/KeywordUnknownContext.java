/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.execution.debug.contexts;

import java.net.URI;
import java.util.Optional;

import org.rf.ide.core.execution.debug.RobotBreakpointSupplier;
import org.rf.ide.core.execution.debug.RunningKeyword;
import org.rf.ide.core.execution.debug.StackFrameContext;
import org.rf.ide.core.testdata.model.FileRegion;


public class KeywordUnknownContext extends KeywordContext {

    private final String errorMessage;

    public KeywordUnknownContext(final String errorMessage) {
        this.errorMessage = errorMessage;
    }

    @Override
    public boolean isErroneous() {
        return true;
    }

    @Override
    public Optional<String> getErrorMessage() {
        return Optional.of(errorMessage);
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
    public StackFrameContext moveTo(final RunningKeyword keyword, final RobotBreakpointSupplier breakpointSupplier) {
        final String errorMsg = String.format(ErrorMessages.executableCallNotFound, keyword.asCall());
        return new ErroneousExecutableCallContext(null, -1, errorMsg);
    }
}
