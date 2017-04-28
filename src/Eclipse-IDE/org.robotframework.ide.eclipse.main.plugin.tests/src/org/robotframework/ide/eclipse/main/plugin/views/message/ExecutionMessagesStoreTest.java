/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.views.message;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.robotframework.ide.eclipse.main.plugin.views.message.ExecutionMessagesStore.ExecutionMessagesStoreListener;

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
    public void storeListenersAreNotified_whenMessageIsAppended() {
        final StringBuilder str1 = new StringBuilder();
        final StringBuilder str2 = new StringBuilder();

        final ExecutionMessagesStoreListener listener1 = msg -> str1.append(msg);
        final ExecutionMessagesStoreListener listener2 = msg -> str2.append(msg);
        
        final ExecutionMessagesStore store = new ExecutionMessagesStore();
        store.addStoreListener(listener1);
        store.append("msg1");
        store.addStoreListener(listener2);
        store.append("msg2");

        assertThat(str1.toString()).isEqualTo("msg1msg2");
        assertThat(str2.toString()).isEqualTo("msg2");
    }

    @Test
    public void storeListenersAreRemoved_whenDisposed() {
        final StringBuilder str1 = new StringBuilder();
        final StringBuilder str2 = new StringBuilder();

        final ExecutionMessagesStoreListener listener1 = msg -> str1.append(msg);
        final ExecutionMessagesStoreListener listener2 = msg -> str2.append(msg);

        final ExecutionMessagesStore store = new ExecutionMessagesStore();
        store.addStoreListener(listener1);
        store.addStoreListener(listener2);
        store.append("msg1");
        store.dispose();
        store.append("msg2");

        assertThat(str1.toString()).isEqualTo("msg1");
        assertThat(str2.toString()).isEqualTo("msg1");
    }

}
