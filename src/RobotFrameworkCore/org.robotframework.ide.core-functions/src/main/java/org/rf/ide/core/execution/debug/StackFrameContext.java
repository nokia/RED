/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.execution.debug;

import java.net.URI;
import java.util.Optional;

import org.rf.ide.core.testdata.model.FileRegion;

public interface StackFrameContext {

    /**
     * Returns true when this context is erroneous (e.g. element associated with context is not
     * found)
     * 
     * @return
     */
    boolean isErroneous();

    /**
     * Gets message with error describing why this context is erroneous.
     * 
     * @return
     */
    Optional<String> getErrorMessage();

    /**
     * Path to file or directory associated with this context
     * 
     * @return Path to the file or directory. Can be empty when no path is associated or it was not
     *         found
     */
    Optional<URI> getAssociatedPath();

    /**
     * Returns file region for which context was created
     * 
     * @return
     */
    Optional<FileRegion> getFileRegion();

    /**
     * Translates current context into a context based on name and type of newly started keyword.
     * 
     * @param keyword
     *            Keyword which is currently in execution
     * @param breakpointSupplier
     *            Supplier which is able to provide breakpoints if possible
     * @return New context
     */
    StackFrameContext moveTo(RunningKeyword keyword, RobotBreakpointSupplier breakpointSupplier);

    /**
     * Provides previous context from which this one was translated.
     * 
     * @return Previous context (may be this context)
     */
    StackFrameContext previousContext();

    /**
     * Gets line breakpoint associated with current context
     * 
     * @return
     */
    Optional<RobotLineBreakpoint> getLineBreakpoint();

}
