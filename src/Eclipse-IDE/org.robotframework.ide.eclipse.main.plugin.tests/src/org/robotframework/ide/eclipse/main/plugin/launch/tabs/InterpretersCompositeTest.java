/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.launch.tabs;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.util.concurrent.atomic.AtomicBoolean;

import org.assertj.core.api.Condition;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.junit.Rule;
import org.junit.Test;
import org.rf.ide.core.executor.SuiteExecutor;
import org.robotframework.ide.eclipse.main.plugin.launch.tabs.InterpretersComposite.InterpreterListener;
import org.robotframework.red.junit.ShellProvider;

public class InterpretersCompositeTest {

    @Rule
    public ShellProvider shellProvider = new ShellProvider();

    @Test
    public void interpretersComposite_inputSettingTest1() {
        final InterpretersComposite composite = new InterpretersComposite(shellProvider.getShell(),
                mock(InterpreterListener.class));
        composite.setInput(true, SuiteExecutor.IronPython);

        assertThat(projectInterpreterButton(composite)).is(selected());

        assertThat(systemInterpreterButton(composite)).isNot(selected());

        assertThat(executorsCombo(composite)).isNot(enabled());
        assertThat(executorsCombo(composite)).has(selection(SuiteExecutor.IronPython.name()));

        assertThat(checkInterpreterButton(composite)).isNot(enabled());

        assertThat(composite.isUsingProjectInterpreter()).isTrue();
        assertThat(composite.getChosenSystemExecutor()).isEqualTo(SuiteExecutor.IronPython);
    }

    @Test
    public void interpretersComposite_inputSettingTest2() {
        final InterpretersComposite composite = new InterpretersComposite(shellProvider.getShell(),
                mock(InterpreterListener.class));
        composite.setInput(false, SuiteExecutor.PyPy);

        assertThat(projectInterpreterButton(composite)).isNot(selected());

        assertThat(systemInterpreterButton(composite)).is(selected());

        assertThat(executorsCombo(composite)).is(enabled());
        assertThat(executorsCombo(composite)).has(selection(SuiteExecutor.PyPy.name()));

        assertThat(checkInterpreterButton(composite)).is(enabled());

        assertThat(composite.isUsingProjectInterpreter()).isFalse();
        assertThat(composite.getChosenSystemExecutor()).isEqualTo(SuiteExecutor.PyPy);
    }

    @Test
    public void whenSystemInterpreterIsChosen_listenerIsNotified() {
        final AtomicBoolean listenerWasCalled = new AtomicBoolean(false);
        final InterpreterListener listener = newExecutor -> listenerWasCalled.set(true);

        final InterpretersComposite composite = new InterpretersComposite(shellProvider.getShell(), listener);
        composite.setInput(true, SuiteExecutor.Jython);

        projectInterpreterButton(composite).setSelection(false);
        projectInterpreterButton(composite).notifyListeners(SWT.Selection, new Event());
        systemInterpreterButton(composite).setSelection(true);
        systemInterpreterButton(composite).notifyListeners(SWT.Selection, new Event());

        assertThat(projectInterpreterButton(composite)).isNot(selected());

        assertThat(systemInterpreterButton(composite)).is(selected());

        assertThat(executorsCombo(composite)).is(enabled());
        assertThat(executorsCombo(composite)).has(selection(SuiteExecutor.Jython.name()));

        assertThat(checkInterpreterButton(composite)).is(enabled());

        assertThat(composite.isUsingProjectInterpreter()).isFalse();
        assertThat(composite.getChosenSystemExecutor()).isEqualTo(SuiteExecutor.Jython);

        assertThat(listenerWasCalled.get()).isTrue();
    }

    @Test
    public void whenProjectInterpreterIsChosen_listenerIsNotified() {
        final AtomicBoolean listenerWasCalled = new AtomicBoolean(false);
        final InterpreterListener listener = newExecutor -> listenerWasCalled.set(true);

        final InterpretersComposite composite = new InterpretersComposite(shellProvider.getShell(), listener);
        composite.setInput(false, SuiteExecutor.PyPy);

        systemInterpreterButton(composite).setSelection(false);
        systemInterpreterButton(composite).notifyListeners(SWT.Selection, new Event());
        projectInterpreterButton(composite).setSelection(true);
        projectInterpreterButton(composite).notifyListeners(SWT.Selection, new Event());

        assertThat(projectInterpreterButton(composite)).is(selected());

        assertThat(systemInterpreterButton(composite)).isNot(selected());

        assertThat(executorsCombo(composite)).isNot(enabled());
        assertThat(executorsCombo(composite)).has(selection(SuiteExecutor.PyPy.name()));

        assertThat(checkInterpreterButton(composite)).isNot(enabled());

        assertThat(composite.isUsingProjectInterpreter()).isTrue();
        assertThat(composite.getChosenSystemExecutor()).isEqualTo(SuiteExecutor.PyPy);

        assertThat(listenerWasCalled.get()).isTrue();
    }

    @Test
    public void whenSystemInterpreterHasChanged_listenerIsNotified() {
        final AtomicBoolean listenerWasCalled = new AtomicBoolean(false);
        final InterpreterListener listener = newExecutor -> listenerWasCalled.set(true);

        final InterpretersComposite composite = new InterpretersComposite(shellProvider.getShell(), listener);
        composite.setInput(false, SuiteExecutor.PyPy);

        executorsCombo(composite).select(SuiteExecutor.allExecutorNames().indexOf(SuiteExecutor.Jython.name()));
        executorsCombo(composite).notifyListeners(SWT.Modify, new Event());

        assertThat(projectInterpreterButton(composite)).isNot(selected());

        assertThat(systemInterpreterButton(composite)).is(selected());

        assertThat(executorsCombo(composite)).is(enabled());
        assertThat(executorsCombo(composite)).has(selection(SuiteExecutor.Jython.name()));

        assertThat(checkInterpreterButton(composite)).is(enabled());

        assertThat(composite.isUsingProjectInterpreter()).isFalse();
        assertThat(composite.getChosenSystemExecutor()).isEqualTo(SuiteExecutor.Jython);

        assertThat(listenerWasCalled.get()).isTrue();
    }

    private static Button projectInterpreterButton(final Composite composite) {
        for (final Control control : composite.getChildren()) {
            if (control instanceof Button) {
                final Button button = (Button) control;
                final String text = button.getText().toLowerCase();
                if (text.startsWith("use") && text.contains("project")) {
                    return button;
                }
            }
        }
        return null;
    }

    private static Button systemInterpreterButton(final Composite composite) {
        for (final Control control : composite.getChildren()) {
            if (control instanceof Button) {
                final Button button = (Button) control;
                final String text = button.getText().toLowerCase();
                if (text.startsWith("use") && !text.contains("project")) {
                    return button;
                }
            }
        }
        return null;
    }

    private static Button checkInterpreterButton(final Composite composite) {
        for (final Control control : composite.getChildren()) {
            if (control instanceof Button) {
                final Button button = (Button) control;
                final String text = button.getText().toLowerCase();
                if (text.contains("check")) {
                    return button;
                }
            }
        }
        return null;
    }

    private static Combo executorsCombo(final Composite composite) {
        for (final Control control : composite.getChildren()) {
            if (control instanceof Combo) {
                return (Combo) control;
            }
        }
        return null;
    }

    private static Condition<? super Button> selected() {
        return new Condition<Button>() {
            @Override
            public boolean matches(final Button button) {
                return button.getSelection();
            }
        };
    }

    private static Condition<? super Control> enabled() {
        return new Condition<Control>() {
            @Override
            public boolean matches(final Control control) {
                return control.isEnabled();
            }
        };
    }

    private static Condition<? super Combo> selection(final String name) {
        return new Condition<Combo>() {

            @Override
            public boolean matches(final Combo combo) {
                return combo.getItem(combo.getSelectionIndex()).equals(name);
            }
        };
    }
}
