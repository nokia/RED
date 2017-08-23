/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.views.message;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class ExecutionMessagesStoreTest {

    @Test
    public void storeIsEmpty_whenCreated() {
        final ExecutionMessagesStore store = new ExecutionMessagesStore();

        assertThat(store.getMessage()).isEmpty();
    }

    @Test
    public void storeProperlySavesMessages_whenTheyAreAppended() {
        final ExecutionMessagesStore store = new ExecutionMessagesStore();
        store.append("msg1");
        store.append("msg2");
        store.append("msg3");
        
        assertThat(store.getMessage()).isEqualTo("msg1msg2msg3");
    }

    @Test
    public void storeRemovesMessages_whenDisposed() {
        final ExecutionMessagesStore store = new ExecutionMessagesStore();
        store.append("msg1");
        store.dispose();
        
        assertThat(store.getMessage()).isEmpty();
    }

    @Test
    public void storeGetsFirsty_whenMessageIsAppended() {
        final ExecutionMessagesStore store = new ExecutionMessagesStore();
        store.append("msg1");
        assertThat(store.checkDirtyAndReset()).isTrue();
        assertThat(store.checkDirtyAndReset()).isFalse();

        store.append("msg2");
        assertThat(store.checkDirtyAndReset()).isTrue();
        assertThat(store.checkDirtyAndReset()).isFalse();
    }
}
