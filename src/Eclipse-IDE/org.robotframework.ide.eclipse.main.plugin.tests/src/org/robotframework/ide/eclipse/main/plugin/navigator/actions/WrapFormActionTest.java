/*
* Copyright 2018 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.robotframework.ide.eclipse.main.plugin.navigator.actions;

import static org.assertj.core.api.Assertions.assertThat;

import org.eclipse.ui.forms.widgets.ScrolledFormText;
import org.junit.Rule;
import org.junit.Test;
import org.robotframework.red.junit.ShellProvider;

public class WrapFormActionTest {

    @Rule
    public ShellProvider shellProvider = new ShellProvider();

    @Test
    public void freshActionIsNotChecked() {
        final ScrolledFormText scrolledFormText = new ScrolledFormText(shellProvider.getShell(), true);

        final WrapFormAction action = new WrapFormAction(scrolledFormText);

        assertThat(action.isChecked()).isFalse();
    }

    @Test
    public void whenRunForTheFirstTimeExpandIsEnabled() {
        final ScrolledFormText scrolledFormText = new ScrolledFormText(shellProvider.getShell(), true);

        final WrapFormAction action = new WrapFormAction(scrolledFormText);

        assertThat(scrolledFormText.getExpandHorizontal()).isFalse();
        assertThat(scrolledFormText.getExpandVertical()).isFalse();

        // simulates press
        action.setChecked(true);
        action.run();

        assertThat(scrolledFormText.getExpandHorizontal()).isTrue();
        assertThat(scrolledFormText.getExpandVertical()).isTrue();

    }

    @Test
    public void whenRunTwoTimesExpandIsDisabled() {
        final ScrolledFormText scrolledFormText = new ScrolledFormText(shellProvider.getShell(), true);

        final WrapFormAction action = new WrapFormAction(scrolledFormText);

        assertThat(scrolledFormText.getExpandHorizontal()).isFalse();
        assertThat(scrolledFormText.getExpandVertical()).isFalse();

        // simulates 2x press
        action.setChecked(true);
        action.run();
        action.setChecked(false);
        action.run();

        assertThat(scrolledFormText.getExpandHorizontal()).isFalse();
        assertThat(scrolledFormText.getExpandVertical()).isFalse();

    }

}
