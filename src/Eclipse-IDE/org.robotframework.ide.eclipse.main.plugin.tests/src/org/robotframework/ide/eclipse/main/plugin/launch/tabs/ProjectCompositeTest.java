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
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.robotframework.red.junit.jupiter.FreshShell;
import org.robotframework.red.junit.jupiter.FreshShellExtension;

@ExtendWith(FreshShellExtension.class)
public class ProjectCompositeTest {

    @FreshShell
    Shell shell;

    @Test
    public void projectComposite_inputSettingTest() {
        final ProjectComposite composite = new ProjectComposite(shell, mock(ModifyListener.class));
        composite.setInput("  someProject ");

        assertThat(projectText(composite)).is(enabled());
        assertThat(checkBrowseButton(composite)).is(enabled());

        assertThat(composite.getSelectedProjectName()).isEqualTo("someProject");
    }

    @Test
    public void whenProjectIsSelected_listenerIsNotified() {
        final AtomicBoolean listenerWasCalled = new AtomicBoolean(false);
        final ModifyListener listener = new ModifyListener() {

            @Override
            public void modifyText(final ModifyEvent e) {
                listenerWasCalled.set(true);
            }
        };

        final ProjectComposite composite = new ProjectComposite(shell, listener);

        projectText(composite).setText("selected");

        assertThat(projectText(composite)).is(enabled());
        assertThat(checkBrowseButton(composite)).is(enabled());

        assertThat(composite.getSelectedProjectName()).isEqualTo("selected");

        assertThat(listenerWasCalled.get()).isTrue();
    }

    private static Text projectText(final Composite composite) {
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
