/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.views.message;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.robotframework.ide.eclipse.main.plugin.RedPreferences;
import org.robotframework.red.junit.jupiter.BooleanPreference;
import org.robotframework.red.junit.jupiter.IntegerPreference;
import org.robotframework.red.junit.jupiter.Managed;
import org.robotframework.red.junit.jupiter.PreferencesExtension;
import org.robotframework.red.junit.jupiter.PreferencesUpdater;

@ExtendWith(PreferencesExtension.class)
public class ExecutionMessagesStoreTest {

    @Managed
    PreferencesUpdater updater;

    private ExecutionMessagesStore store;

    @BeforeEach
    public void beforeTest() {
        store = new ExecutionMessagesStore();
    }

    @AfterEach
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

    @BooleanPreference(key = RedPreferences.LIMIT_MSG_LOG_OUTPUT, value = true)
    @IntegerPreference(key = RedPreferences.LIMIT_MSG_LOG_LENGTH, value = 5)
    @Test
    public void storeOnlyRemembersLastCharacters_whenCharactersLimitIsSet() {
        store.open();
        store.append("message");

        assertThat(store.getMessage()).isEqualTo("ssage");

        store.append("1");
        assertThat(store.getMessage()).isEqualTo("sage1");

        updater.setValue(RedPreferences.LIMIT_MSG_LOG_LENGTH, 3);
        assertThat(store.getMessage()).isEqualTo("sage1");

        store.append("2");
        assertThat(store.getMessage()).isEqualTo("e12");
    }
}
