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

import com.google.common.annotations.VisibleForTesting;

public class ForLoopContext extends DefaultContext {

    private final ExecutableWithDescriptor forLoopExecutable;

    private final URI locationUri;

    private final int line;

    private final String errorMessage;

    public static StackFrameContext findContextForLoop(final StackFrameContext currentContext) {
        final URI location = currentContext.getAssociatedPath().orElse(null);
        final Integer line = currentContext.getFileRegion()
                .map(FileRegion::getStart)
                .map(FilePosition::getLine)
                .orElse(-1);

        if (currentContext instanceof ExecutableCallContext) {
            final ExecutableCallContext execContext = (ExecutableCallContext) currentContext;
            final ExecutableWithDescriptor currentElement = execContext.currentElement();

            if (currentElement.isLoopExecutable()) {
                return new ForLoopContext(currentElement, location, line,
                        currentContext.getErrorMessage().orElse(null));

            } else {
                final String errorMsg = String.format(ErrorMessages.executableCallNotFound_foundCallButFor,
                        currentElement.getCalledKeywordName());
                return new ForLoopContext(location, line, errorMsg);
            }
        } else if (currentContext instanceof ErroneousExecutableCallContext) {
            return new ForLoopContext(location, line, currentContext.getErrorMessage().get());
        }
        throw new IllegalDebugContextStateException(
                "For loop can only be called when already context was moved to executable call");
    }

    @VisibleForTesting
    ForLoopContext(final URI locationUri, final int line, final String errorMsg) {
        this(null, locationUri, line, errorMsg);
    }

    @VisibleForTesting
    ForLoopContext(final ExecutableWithDescriptor forLoopExecutable, final URI locationUri, final int line,
            final String errorMsg) {
        this.forLoopExecutable = forLoopExecutable;
        this.locationUri = locationUri;
        this.line = line;
        this.errorMessage = errorMsg;
    }

    public ExecutableWithDescriptor currentElement() {
        return forLoopExecutable;
    }

    @Override
    public boolean isErroneous() {
        return errorMessage != null;
    }

    @Override
    public Optional<String> getErrorMessage() {
        return Optional.ofNullable(errorMessage);
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
        return this;
    }
}