/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.execution.server;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.util.Optional;

import org.junit.Test;
import org.rf.ide.core.execution.agent.event.VersionsEvent;
import org.rf.ide.core.execution.agent.event.VersionsEvent.VersionsEventResponder;


public class TestsPidReaderTest {

    @Test
    public void pidIsNegative_whenItIsNotStoredInEvent() {
        final VersionsEvent event = new VersionsEvent(mock(VersionsEventResponder.class), "", "3.6", "3.0", 1,
                Optional.empty());

        final TestsPidReader reader = new TestsPidReader();
        reader.handleVersions(event);
        
        assertThat(reader.getPid()).isEqualTo(-1);
    }

    @Test
    public void pidIsReturnedAsIs_whenItIsStoredInEvent() {
        final VersionsEvent event = new VersionsEvent(mock(VersionsEventResponder.class), "", "3.6", "3.0", 1,
                Optional.of(42L));

        final TestsPidReader reader = new TestsPidReader();
        reader.handleVersions(event);

        assertThat(reader.getPid()).isEqualTo(42);
    }

}
