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
import org.rf.ide.core.testdata.model.FilePosition;
import org.rf.ide.core.testdata.model.FileRegion;


class ErroneousExecutableCallContext extends DefaultContext {

    private final URI locationUri;

    private final int line;

    private final String errorMessage;

    ErroneousExecutableCallContext(final URI locationUri, final int line, final String errorMessage) {
        this.locationUri = locationUri;
        this.line = line;
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
        return Optional.ofNullable(locationUri);
    }

    @Override
    public Optional<FileRegion> getFileRegion() {
        return Optional.of(new FileRegion(new FilePosition(line, -1, -1), new FilePosition(line, -1, -1)));
    }

    @Override
    public StackFrameContext moveTo(final RunningKeyword keyword, final RobotBreakpointSupplier breakpointSupplier) {
        final String errorMsg = String.format(ErrorMessages.executableCallNotFound, keyword.asCall());
        return new ErroneousExecutableCallContext(locationUri, line, errorMsg);
    }
}
