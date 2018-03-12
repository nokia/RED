/*
* Copyright 2017 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.rf.ide.core.execution.debug;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class StackFrameMarkerTest {

    @Test
    public void ensuringParsingTest() {
        assertThat(StackFrameMarker.valueOf("ERROR")).isEqualTo(StackFrameMarker.ERROR);
        assertThat(StackFrameMarker.valueOf("STEPPING")).isEqualTo(StackFrameMarker.STEPPING);
    }
}
