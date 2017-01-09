/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.source.assist;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.TextPresentation;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Shell;
import org.junit.Rule;
import org.junit.Test;
import org.robotframework.red.junit.ShellProvider;

public class SuiteSourceContextInformationValidatorTest {

    @Rule
    public ShellProvider shellProvider = new ShellProvider();

    // this behavior is subject to change maybe
    @Test
    public void presentationIsNotUpdated() {
        final Shell shell = shellProvider.getShell();
        final StyledText textWidget = new StyledText(shell, SWT.SINGLE);
        textWidget.setText("some content of widget");

        final IContextInformation contextInformation = mock(IContextInformation.class);
        final ITextViewer viewer = mock(ITextViewer.class);
        when(viewer.getTextWidget()).thenReturn(textWidget);
        final TextPresentation presentation = mock(TextPresentation.class);

        final SuiteSourceContextInformationValidator validator = new SuiteSourceContextInformationValidator();
        validator.install(contextInformation, viewer, 10);
        validator.updatePresentation(10, presentation);

        verifyZeroInteractions(presentation);
    }

    @Test
    public void contextInformationIsInvalid_whenInDifferentLine() {
        final Shell shell = shellProvider.getShell();
        final StyledText textWidget = new StyledText(shell, SWT.SINGLE);
        textWidget.setText("line\nline\nline\nline\nline\n");

        final IContextInformation contextInformation = mock(IContextInformation.class);
        final ITextViewer viewer = mock(ITextViewer.class);
        when(viewer.getTextWidget()).thenReturn(textWidget);
        final TextPresentation presentation = mock(TextPresentation.class);

        final SuiteSourceContextInformationValidator validator = new SuiteSourceContextInformationValidator();
        validator.install(contextInformation, viewer, 12);

        for (int offset = 0; offset < textWidget.getCharCount(); offset++) {
            if (offset < 10 || offset >= 15) {
                assertThat(validator.isContextInformationValid(offset)).isFalse();
            }
        }

        verifyZeroInteractions(presentation);
    }

    @Test
    public void contextInformationIsInvalid_whenInSameLineButBeforeInstalledPosition() {
        final Shell shell = shellProvider.getShell();
        final StyledText textWidget = new StyledText(shell, SWT.SINGLE);
        textWidget.setText("line\nline\nline\nline\nline\n");

        final IContextInformation contextInformation = mock(IContextInformation.class);
        final ITextViewer viewer = mock(ITextViewer.class);
        when(viewer.getTextWidget()).thenReturn(textWidget);
        final TextPresentation presentation = mock(TextPresentation.class);

        final SuiteSourceContextInformationValidator validator = new SuiteSourceContextInformationValidator();
        validator.install(contextInformation, viewer, 12);

        assertThat(validator.isContextInformationValid(10)).isFalse();
        assertThat(validator.isContextInformationValid(11)).isFalse();

        verifyZeroInteractions(presentation);
    }

    @Test
    public void contextInformationIsValid_whenInSameLineButAfterInstalledPosition() {
        final Shell shell = shellProvider.getShell();
        final StyledText textWidget = new StyledText(shell, SWT.SINGLE);
        textWidget.setText("line\nline\nline\nline\nline\n");

        final IContextInformation contextInformation = mock(IContextInformation.class);
        final ITextViewer viewer = mock(ITextViewer.class);
        when(viewer.getTextWidget()).thenReturn(textWidget);
        final TextPresentation presentation = mock(TextPresentation.class);

        final SuiteSourceContextInformationValidator validator = new SuiteSourceContextInformationValidator();
        validator.install(contextInformation, viewer, 12);

        assertThat(validator.isContextInformationValid(12)).isTrue();
        assertThat(validator.isContextInformationValid(13)).isTrue();
        assertThat(validator.isContextInformationValid(14)).isTrue();

        verifyZeroInteractions(presentation);
    }

}
