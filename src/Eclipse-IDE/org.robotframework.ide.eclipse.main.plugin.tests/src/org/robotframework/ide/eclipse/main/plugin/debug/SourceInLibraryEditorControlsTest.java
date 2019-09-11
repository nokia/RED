/*
* Copyright 2017 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.robotframework.ide.eclipse.main.plugin.debug;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.util.List;

import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.forms.widgets.FormText;
import org.eclipse.ui.forms.widgets.ScrolledFormText;
import org.junit.Rule;
import org.junit.Test;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.debug.SourceInLibraryEditorInput.SourceOfStackFrameInLibrary;
import org.robotframework.red.graphics.ImagesManager;
import org.robotframework.red.junit.Controls;
import org.robotframework.red.junit.ShellProvider;

public class SourceInLibraryEditorControlsTest {

    @Rule
    public ShellProvider shellProvider = new ShellProvider();

    @Test
    public void controlsAreConstructedProperly() {
        final Composite shell = shellProvider.getShell();
        shell.setLayout(new FillLayout());

        final SourceInLibraryEditorControls controls = new SourceInLibraryEditorControls(mock(IWorkbenchPage.class));
        controls.construct(shell);

        final List<Control> controlsInside = Controls.getControls(shell);

        assertThat(controls.getParent()).isSameAs(shell);
        assertThat(controlsInside).hasSize(3);
        assertThat(controlsInside.get(0)).isInstanceOf(CLabel.class);
        assertThat(controlsInside.get(1)).isInstanceOf(ScrolledFormText.class);
        assertThat(controlsInside.get(2)).isInstanceOf(FormText.class);
    }

    @Test
    public void inputIsSetIntoControls() {
        final SourceInLibraryEditorInput input = new SourceInLibraryEditorInput(
                new SourceOfStackFrameInLibrary("kw", null));

        final Composite shell = shellProvider.getShell();
        shell.setLayout(new FillLayout());

        final SourceInLibraryEditorControls controls = new SourceInLibraryEditorControls(mock(IWorkbenchPage.class));
        controls.construct(shell);
        controls.setInput(input);

        final List<Control> controlsInside = Controls.getControls(shell);
        final CLabel titleLabel = (CLabel) controlsInside.get(0);

        assertThat(titleLabel.getImage()).isSameAs(ImagesManager.getImage(RedImages.getBigKeywordImage()));
        assertThat(titleLabel.getText()).isEqualTo("Keyword kw contained in library");
    }

    @Test
    public void controlsAreDisposedProperly() {
        final Composite shell = shellProvider.getShell();
        shell.setLayout(new FillLayout());

        final SourceInLibraryEditorControls controls = new SourceInLibraryEditorControls(mock(IWorkbenchPage.class));
        controls.construct(shell);
        controls.dispose();

        for (final Control control : Controls.getControls(shell)) {
            assertThat(control.isDisposed()).isTrue();
        }
    }
}
