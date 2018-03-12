/*
* Copyright 2017 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.robotframework.ide.eclipse.main.plugin.debug;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.junit.Rule;
import org.junit.Test;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.debug.RedDebuggerAssistantEditorWrapper.RedDebuggerAssistantEditorInput;
import org.robotframework.red.graphics.ImagesManager;
import org.robotframework.red.junit.ShellProvider;

public class SourceNotFoundEditorControlsTest {

    @Rule
    public ShellProvider shellProvider = new ShellProvider();

    @Test
    public void controlsAreConstructedProperly() {
        final Composite shell = shellProvider.getShell();
        shell.setLayout(new FillLayout());

        final SourceNotFoundEditorControls controls = new SourceNotFoundEditorControls();
        controls.construct(shell);

        final List<Control> controlsInside = getControls(shell);

        assertThat(controls.getParent()).isSameAs(shell);
        assertThat(controlsInside).hasSize(2);
        assertThat(controlsInside.get(0)).isInstanceOf(CLabel.class);
        assertThat(controlsInside.get(1)).isInstanceOf(StyledText.class);
    }

    @Test
    public void inputIsSetIntoControls() {
        final RedDebuggerAssistantEditorInput input = mock(RedDebuggerAssistantEditorInput.class);
        when(input.getTitle()).thenReturn("title");
        when(input.getTitleImageDescriptor()).thenReturn(RedImages.getElementImage());
        when(input.getDetailedInformation()).thenReturn("info");

        final Composite shell = shellProvider.getShell();
        shell.setLayout(new FillLayout());

        final SourceNotFoundEditorControls controls = new SourceNotFoundEditorControls();
        controls.construct(shell);
        controls.setInput(input);

        final List<Control> controlsInside = getControls(shell);
        final CLabel titleLabel = (CLabel) controlsInside.get(0);
        final StyledText infoText = (StyledText) controlsInside.get(1);

        assertThat(titleLabel.getImage()).isSameAs(ImagesManager.getImage(RedImages.getElementImage()));
        assertThat(titleLabel.getText()).isEqualTo("title");
        assertThat(infoText.getText()).isEqualTo("info");
    }

    @Test
    public void controlsAreDisposedProperly() {
        final Composite shell = shellProvider.getShell();
        shell.setLayout(new FillLayout());

        final SourceNotFoundEditorControls controls = new SourceNotFoundEditorControls();
        controls.construct(shell);
        controls.dispose();

        for (final Control control : getControls(shell)) {
            assertThat(control.isDisposed()).isTrue();
        }
    }

    private static List<Control> getControls(final Control control) {
        final List<Control> controls = new ArrayList<>();
        getControls(controls, control);
        return controls;
    }

    private static void getControls(final List<Control> controls, final Control control) {
        if (control instanceof Composite) {
            if (control.getClass() != Composite.class && control.getClass() != Shell.class) {
                controls.add(control);
            }
            final Composite composite = (Composite) control;
            for (final Control child : composite.getChildren()) {
                getControls(controls, child);
            }
        } else {
            controls.add(control);
        }
    }
}
