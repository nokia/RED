/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.execution.debug;

import java.net.URI;
import java.util.Optional;

@FunctionalInterface
public interface RobotBreakpointSupplier {

    public Optional<RobotLineBreakpoint> breakpointFor(URI location, int line);

}
