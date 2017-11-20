/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.views.message;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.robotframework.ide.eclipse.main.plugin.RedPreferences;
import org.robotframework.red.junit.PreferenceUpdater;

public class ExecutionMessagesStoreTest {

    @Rule
    public PreferenceUpdater preferenceUpdater = new PreferenceUpdater();

    private ExecutionMessagesStore store;

    @Before
    public void beforeTest() {
        store = new ExecutionMessagesStore();
    }

    @After
    public void afterTest() {
        store.dispose();
    }

    @Test
    public void storeIsEmpty_whenCreated() {
        assertThat(store.getMessage()).isEmpty();
    }

    @Test
    public void storeProperlySavesMessages_whenTheyAreAppended() {
        store.open();

        store.append("msg1");
        store.append("msg2");
        store.append("msg3");
        
        assertThat(store.getMessage()).isEqualTo("msg1msg2msg3");
    }

    @Test
    public void storeRemovesMessages_whenDisposed() {
        store.open();

        store.append("msg1");
        store.dispose();
        
        assertThat(store.getMessage()).isEmpty();
    }

    @Test
    public void storeGetsDirty_whenMessageIsAppended() {
        store.open();

        store.append("msg1");
        assertThat(store.checkDirtyAndReset()).isTrue();
        assertThat(store.checkDirtyAndReset()).isFalse();

        store.append("msg2");
        assertThat(store.checkDirtyAndReset()).isTrue();
        assertThat(store.checkDirtyAndReset()).isFalse();
    }

    @Test
    public void storeOnlyRemembersLastCharacters_whenCharactersLimitIsSet() {
        preferenceUpdater.setValue(RedPreferences.LIMIT_MSG_LOG_OUTPUT, true);
        preferenceUpdater.setValue(RedPreferences.LIMIT_MSG_LOG_LENGTH, 5);

        store.open();
        store.append("message");

        assertThat(store.getMessage()).isEqualTo("ssage");

        store.append("1");
        assertThat(store.getMessage()).isEqualTo("sage1");

        preferenceUpdater.setValue(RedPreferences.LIMIT_MSG_LOG_LENGTH, 3);
        assertThat(store.getMessage()).isEqualTo("sage1");

        store.append("2");
        assertThat(store.getMessage()).isEqualTo("e12");
    }
}
