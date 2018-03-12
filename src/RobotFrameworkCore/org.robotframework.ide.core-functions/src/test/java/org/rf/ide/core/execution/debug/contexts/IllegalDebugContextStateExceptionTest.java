/*
* Copyright 2017 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.rf.ide.core.execution.debug.contexts;

import org.junit.Test;

public class IllegalDebugContextStateExceptionTest {

    @Test
    public void theExceptionIsRuntime() {
        try {
            throw new IllegalDebugContextStateException("msg");
        } catch (final RuntimeException e) {
            // that's expected
        }
    }

}
