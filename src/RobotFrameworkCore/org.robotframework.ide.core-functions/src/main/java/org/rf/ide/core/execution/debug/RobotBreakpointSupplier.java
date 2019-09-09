/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.execution.debug;

import java.net.URI;
import java.util.Optional;

public class RobotBreakpointSupplier {

    @SuppressWarnings("unused")
    public Optional<RobotBreakpoint> lineBreakpointFor(final URI location, final int line) {
        return Optional.empty();
    }

    @SuppressWarnings("unused")
    public Optional<RobotBreakpoint> keywordFailBreakpointFor(final String keywordName) {
        return Optional.empty();
    }
}
