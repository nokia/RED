/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.launch.tabs;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.util.concurrent.atomic.AtomicBoolean;

import org.assertj.core.api.Condition;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.junit.Rule;
import org.junit.Test;
import org.robotframework.red.junit.ShellProvider;

public class ExecutorScriptCompositeTest {

    @Rule
    public ShellProvider shellProvider = new ShellProvider();

    @Test
    public void executorScriptComposite_inputSettingTest() {
        final ExecutorScriptComposite composite = new ExecutorScriptComposite(shellProvider.getShell(),
                mock(ModifyListener.class), new String[] {});
        composite.setInput(" path ");

        assertThat(scriptPathText(composite)).is(enabled());
        assertThat(checkBrowseButton(composite)).is(enabled());

        assertThat(composite.getSelectedScriptPath()).isEqualTo("path");
    }

    @Test
    public void whenScriptPathIsSelected_listenerIsNotified() {
        final AtomicBoolean listenerWasCalled = new AtomicBoolean(false);
        final ModifyListener listener = new ModifyListener() {

            @Override
            public void modifyText(final ModifyEvent e) {
                listenerWasCalled.set(true);
            }
        };

        final ExecutorScriptComposite composite = new ExecutorScriptComposite(shellProvider.getShell(), listener,
                new String[] {});

        scriptPathText(composite).setText("selected");

        assertThat(scriptPathText(composite)).is(enabled());
        assertThat(checkBrowseButton(composite)).is(enabled());

        assertThat(composite.getSelectedScriptPath()).isEqualTo("selected");

        assertThat(listenerWasCalled.get()).isTrue();
    }

    private static Text scriptPathText(final Composite composite) {
        for (final Control control : composite.getChildren()) {
            if (control instanceof Text) {
                return (Text) control;
            }
        }
        return null;
    }

    private static Button checkBrowseButton(final Composite composite) {
        for (final Control control : composite.getChildren()) {
            if (control instanceof Button) {
                final Button button = (Button) control;
                final String text = button.getText().toLowerCase();
                if (text.contains("browse...")) {
                    return button;
                }
            }
        }
        return null;
    }

    private static Condition<? super Control> enabled() {
        return new Condition<Control>() {

            @Override
            public boolean matches(final Control control) {
                return control.isEnabled();
            }
        };
    }
}
