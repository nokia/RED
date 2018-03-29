/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.launch.tabs;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.util.List;
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

public class ExecutableFileCompositeTest {

    @Rule
    public ShellProvider shellProvider = new ShellProvider();

    @Test
    public void executableFileCompositeComposite_inputSettingTest() {
        final ExecutableFileComposite composite = new ExecutableFileComposite(shellProvider.getShell(),
                mock(ModifyListener.class));
        composite.setInput(" path ");

        assertThat(executableFilePathText(composite)).is(enabled());
        final List<Button> browseButtons = getBrowseButtons(composite);
        assertThat(browseButtons.stream().map(Button::getText)).containsExactly("Workspace...", "File system...",
                "Variables...");
        assertThat(browseButtons).allSatisfy(button -> assertThat(button).is(enabled()));

        assertThat(composite.getSelectedExecutableFilePath()).isEqualTo("path");
    }

    @Test
    public void whenExecutableFilePathIsSelected_listenerIsNotified() {
        final AtomicBoolean listenerWasCalled = new AtomicBoolean(false);
        final ModifyListener listener = e -> listenerWasCalled.set(true);

        final ExecutableFileComposite composite = new ExecutableFileComposite(shellProvider.getShell(), listener);

        executableFilePathText(composite).setText("selected");

        assertThat(executableFilePathText(composite)).is(enabled());
        final List<Button> browseButtons = getBrowseButtons(composite);
        assertThat(browseButtons.stream().map(Button::getText)).containsExactly("Workspace...", "File system...",
                "Variables...");
        assertThat(browseButtons).allSatisfy(button -> assertThat(button).is(enabled()));

        assertThat(composite.getSelectedExecutableFilePath()).isEqualTo("selected");

        assertThat(listenerWasCalled.get()).isTrue();
    }

    private static Text executableFilePathText(final Composite composite) {
        return Stream.of(composite.getChildren())
                .filter(Text.class::isInstance)
                .map(Text.class::cast)
                .findFirst()
                .get();
    }

    private static List<Button> getBrowseButtons(final Composite composite) {
        return Stream.of(composite.getChildren())
                .filter(Composite.class::isInstance)
                .map(Composite.class::cast)
                .flatMap(c -> Stream.of(c.getChildren()))
                .filter(Button.class::isInstance)
                .map(Button.class::cast)
                .collect(toList());
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
