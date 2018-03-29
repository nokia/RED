/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.launch.tabs;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

import org.assertj.core.api.Condition;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.junit.Rule;
import org.junit.Test;
import org.robotframework.red.junit.ShellProvider;

public class AdditionalArgumentsCompositeTest {

    @Rule
    public ShellProvider shellProvider = new ShellProvider();

    @Test
    public void additionalArgumentsComposite_inputSettingTest() {
        final AdditionalArgumentsComposite composite = new AdditionalArgumentsComposite(shellProvider.getShell(),
                mock(ModifyListener.class));
        composite.setInput(" arg1 arg2 ");

        assertThat(argumentsText(composite)).is(enabled());
        assertThat(getBrowseButton(composite).getText()).isEqualTo("Variables...");
        assertThat(getBrowseButton(composite)).is(enabled());

        assertThat(composite.getArguments()).isEqualTo("arg1 arg2");
    }

    @Test
    public void whenAdditionalArgumentsAreSelected_listenerIsNotified() {
        final AtomicBoolean listenerWasCalled = new AtomicBoolean(false);
        final ModifyListener listener = e -> listenerWasCalled.set(true);

        final AdditionalArgumentsComposite composite = new AdditionalArgumentsComposite(shellProvider.getShell(),
                listener);

        argumentsText(composite).setText("arg");

        assertThat(argumentsText(composite)).is(enabled());
        assertThat(getBrowseButton(composite).getText()).isEqualTo("Variables...");
        assertThat(getBrowseButton(composite)).is(enabled());

        assertThat(composite.getArguments()).isEqualTo("arg");

        assertThat(listenerWasCalled.get()).isTrue();
    }

    private static Text argumentsText(final Composite composite) {
        return Stream.of(composite.getChildren())
                .filter(Text.class::isInstance)
                .map(Text.class::cast)
                .findFirst()
                .get();
    }

    private static Button getBrowseButton(final Composite composite) {
        return Stream.of(composite.getChildren())
                .filter(Button.class::isInstance)
                .map(Button.class::cast)
                .findFirst()
                .get();
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
