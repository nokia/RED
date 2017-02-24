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
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;
import org.junit.Rule;
import org.junit.Test;
import org.robotframework.red.junit.ShellProvider;

public class ProjectCompositeTest {

    @Rule
    public ShellProvider shellProvider = new ShellProvider();

    @Test
    public void projectComposite_inputSettingTest() {
        final ProjectComposite composite = new ProjectComposite(shellProvider.getShell(), mock(ModifyListener.class));
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

        final ProjectComposite composite = new ProjectComposite(shellProvider.getShell(), listener);

        projectText(composite).setText("selected");

        assertThat(projectText(composite)).is(enabled());
        assertThat(checkBrowseButton(composite)).is(enabled());

        assertThat(composite.getSelectedProjectName()).isEqualTo("selected");

        assertThat(listenerWasCalled.get()).isTrue();
    }

    @Test
    public void projectCompositeGroupCanBeDisposed() {
        final Group group = new Group(shellProvider.getShell(), SWT.NONE);
        final ProjectComposite composite = new ProjectComposite(group, mock(ModifyListener.class));
        final Composite parent = group.getParent();

        composite.disposeGroup();

        assertThat(parent).isNot(disposed());
        assertThat(group).is(disposed());
        assertThat(composite).is(disposed());
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

    private static Condition<? super Control> disposed() {
        return new Condition<Control>() {

            @Override
            public boolean matches(final Control control) {
                return control.isDisposed();
            }
        };
    }
}
