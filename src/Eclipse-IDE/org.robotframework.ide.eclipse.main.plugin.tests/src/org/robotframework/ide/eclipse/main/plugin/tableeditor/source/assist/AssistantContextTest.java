/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.source.assist;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.bindings.keys.KeySequence;
import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.swt.SWT;
import org.junit.Test;
import org.robotframework.ide.eclipse.main.plugin.RedPreferences;
import org.robotframework.ide.eclipse.main.plugin.mockmodel.RobotSuiteFileCreator;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.assist.AssistantContext.AssistPreferences;

public class AssistantContextTest {

    @Test
    public void contextProvidesModelSuppliedByGivenSupplier() {
        final RobotSuiteFile model = new RobotSuiteFileCreator().build();

        assertThat(createContext(null).getModel()).isNull();
        assertThat(createContext(model).getModel()).isSameAs(model);
    }

    @Test
    public void contextProperlyRecognizesTsvModel() {
        final RobotSuiteFile robotModel = new RobotSuiteFileCreator().buildReadOnly();
        final RobotSuiteFile tsvModel = new RobotSuiteFileCreator().buildReadOnlyTsv();

        assertThat(createContext(tsvModel).isTsvFile()).isTrue();
        assertThat(createContext(robotModel).isTsvFile()).isFalse();
    }

    @Test
    public void contextReturnsFileUsedByModel() {
        final RobotSuiteFile model = spy(new RobotSuiteFileCreator().build());
        final IFile file = mock(IFile.class);
        when(model.getFile()).thenReturn(file);

        assertThat(createContext(model).getFile()).isSameAs(file);
    }

    @Test
    public void contextCachesRobotFormatSeparatorPreference() {
        final RobotSuiteFile model = new RobotSuiteFileCreator().buildReadOnly();
        final MockRedPreferences preferences = spy(new MockRedPreferences("  "));

        final AssistantContext context = createContext(model, preferences);
        for (int i = 0; i < 10; i++) {
            assertThat(context.getSeparatorToFollow()).isEqualTo("  ");
        }

        preferences.setSeparatorToUseInRobot(" | ");
        for (int i = 0; i < 10; i++) {
            assertThat(context.getSeparatorToFollow()).isEqualTo("  ");
        }

        context.refreshPreferences();
        for (int i = 0; i < 10; i++) {
            assertThat(context.getSeparatorToFollow()).isEqualTo(" | ");
        }
        verify(preferences, times(2)).getSeparatorToUse(false);
    }

    @Test
    public void contextCachesTsvFormatSeparatorPreference() {
        final RobotSuiteFile model = new RobotSuiteFileCreator().buildReadOnlyTsv();
        final MockRedPreferences preferences = spy(new MockRedPreferences("\t"));

        final AssistantContext context = createContext(model, preferences);
        for (int i = 0; i < 10; i++) {
            assertThat(context.getSeparatorToFollow()).isEqualTo("\t");
        }

        preferences.setSeparatorToUseInTsv("\t\t");
        for (int i = 0; i < 10; i++) {
            assertThat(context.getSeparatorToFollow()).isEqualTo("\t");
        }

        context.refreshPreferences();
        for (int i = 0; i < 10; i++) {
            assertThat(context.getSeparatorToFollow()).isEqualTo("\t\t");
        }
        verify(preferences, times(2)).getSeparatorToUse(true);
    }

    @Test
    public void contextCachesAutoActivationCharsPreference() {
        final RobotSuiteFile model = new RobotSuiteFileCreator().buildReadOnlyTsv();
        final MockRedPreferences preferences = spy(new MockRedPreferences("\t", new char[] { 'a', 'b', 'c' }));

        final AssistantContext context = createContext(model, preferences);
        for (int i = 0; i < 10; i++) {
            assertThat(context.getAssistantAutoActivationChars()).containsExactly('a', 'b', 'c');
        }

        preferences.setAssistantAutoActivationChars(new char[] { 'x', 'y', 'z' });
        for (int i = 0; i < 10; i++) {
            assertThat(context.getAssistantAutoActivationChars()).containsExactly('a', 'b', 'c');
        }

        context.refreshPreferences();
        for (int i = 0; i < 10; i++) {
            assertThat(context.getAssistantAutoActivationChars()).containsExactly('x', 'y', 'z');
        }
        verify(preferences, times(2)).getAssistantAutoActivationChars();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void contextReturnsActivationTrigger() {
        final KeySequence trigger = KeySequence.getInstance(KeyStroke.getInstance(SWT.CTRL, '9'));
        assertThat(new AssistantContext(null, null, trigger).getActivationTrigger()).isSameAs(trigger);
    }

    private AssistantContext createContext(final RobotSuiteFile model) {
        return new AssistantContext(null, () -> model,
                KeySequence.getInstance(KeyStroke.getInstance(SWT.CTRL, SWT.SPACE)));
    }

    private AssistantContext createContext(final RobotSuiteFile model, final RedPreferences redPreferences) {
        return new AssistantContext(null, () -> model,
                KeySequence.getInstance(KeyStroke.getInstance(SWT.CTRL, SWT.SPACE)),
                new AssistPreferences(redPreferences));
    }
}
