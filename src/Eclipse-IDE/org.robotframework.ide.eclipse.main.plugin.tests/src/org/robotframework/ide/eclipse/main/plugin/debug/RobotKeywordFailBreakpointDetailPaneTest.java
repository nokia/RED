/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.debug;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchPartSite;
import org.junit.Test;
import org.robotframework.ide.eclipse.main.plugin.debug.model.RobotKeywordFailBreakpoint;
import org.robotframework.red.junit.Controls;

public class RobotKeywordFailBreakpointDetailPaneTest {

    @Test
    public void panePropertiesTest() {
        final RobotKeywordFailBreakpointDetailPane pane = new RobotKeywordFailBreakpointDetailPane();

        assertThat(pane.getID()).isEqualTo(RobotKeywordFailBreakpointDetailPane.ID);
        assertThat(pane.getName()).isEqualTo(RobotKeywordFailBreakpointDetailPane.NAME);
        assertThat(pane.getDescription()).isEqualTo(RobotKeywordFailBreakpointDetailPane.DESCRIPTION);
        assertThat(pane.isDirty()).isFalse();
    }

    @Test
    public void checkControlsStateAfterCreation() {
        final Shell shell = new Shell(Display.getDefault());
        shell.open();

        final RobotKeywordFailBreakpointDetailPane pane = new RobotKeywordFailBreakpointDetailPane();

        pane.init(mock(IWorkbenchPartSite.class));
        pane.createControl(shell);

        assertThat(pane.setFocus()).isFalse();

        final List<Control> controls = Controls.getControls(shell);
        assertThat(controls).hasSize(4);

        assertThat(controls.get(0)).isInstanceOf(Button.class);
        assertThat(((Button) controls.get(0)).getStyle() & SWT.CHECK).isEqualTo(SWT.CHECK);
        assertThat(((Button) controls.get(0)).isEnabled()).isTrue();
        assertThat(((Button) controls.get(0)).getSelection()).isFalse();

        assertThat(controls.get(1)).isInstanceOf(Text.class);
        assertThat(((Text) controls.get(1)).isEnabled()).isFalse();
        assertThat(((Text) controls.get(1)).getText()).isEmpty();

        assertThat(controls.get(2)).isInstanceOf(Label.class);
        assertThat(((Label) controls.get(2)).isEnabled()).isFalse();
        assertThat(((Label) controls.get(2)).getText()).isEqualTo("Keyword pattern:");

        assertThat(controls.get(3)).isInstanceOf(Text.class);
        assertThat(((Text) controls.get(3)).isEnabled()).isFalse();
        assertThat(((Text) controls.get(3)).getText()).isEmpty();

        shell.close();
        shell.dispose();
    }

    @Test
    public void checkingAndUncheckingHitCountButtonEnablesAndDisablesText_alsoPaneGetsDirty() {
        final Shell shell = new Shell(Display.getDefault());
        shell.open();

        final RobotKeywordFailBreakpointDetailPane pane = new RobotKeywordFailBreakpointDetailPane();

        pane.init(mock(IWorkbenchPartSite.class));
        pane.createControl(shell);

        final List<Control> controls = Controls.getControls(shell);
        final Button hitCountButton = (Button) controls.get(0);
        final Text hitCountText = (Text) controls.get(1);

        assertThat(pane.isDirty()).isFalse();
        assertThat(hitCountText.isEnabled()).isFalse();

        hitCountButton.setSelection(true);
        hitCountButton.notifyListeners(SWT.Selection, new Event());

        assertThat(pane.isDirty()).isTrue();
        assertThat(hitCountText.isEnabled()).isTrue();

        pane.setDirty(false);

        hitCountButton.setSelection(false);
        hitCountButton.notifyListeners(SWT.Selection, new Event());

        assertThat(pane.isDirty()).isTrue();
        assertThat(hitCountText.isEnabled()).isFalse();

        shell.close();
        shell.dispose();
    }

    @Test
    public void modifyingHitCountTextMakesPaneDirty() {
        final Shell shell = new Shell(Display.getDefault());
        shell.open();

        final RobotKeywordFailBreakpointDetailPane pane = new RobotKeywordFailBreakpointDetailPane();

        pane.init(mock(IWorkbenchPartSite.class));
        pane.createControl(shell);

        final List<Control> controls = Controls.getControls(shell);
        final Button hitCountButton = (Button) controls.get(0);
        final Text hitCountText = (Text) controls.get(1);

        assertThat(pane.isDirty()).isFalse();

        hitCountButton.setSelection(true);
        hitCountButton.notifyListeners(SWT.Selection, new Event());

        hitCountText.setText("42");
        hitCountText.notifyListeners(SWT.Modify, new Event());

        assertThat(pane.isDirty()).isTrue();

        shell.close();
        shell.dispose();
    }

    @Test
    public void modifyingPatternMakesPaneDirty() {
        final Shell shell = new Shell(Display.getDefault());
        shell.open();

        final RobotKeywordFailBreakpointDetailPane pane = new RobotKeywordFailBreakpointDetailPane();

        pane.init(mock(IWorkbenchPartSite.class));
        pane.createControl(shell);

        final List<Control> controls = Controls.getControls(shell);
        final Text patternText = (Text) controls.get(3);

        assertThat(pane.isDirty()).isFalse();

        patternText.setText("Keyword Pattern");
        patternText.notifyListeners(SWT.Modify, new Event());

        assertThat(pane.isDirty()).isTrue();

        shell.close();
        shell.dispose();
    }

    @Test
    public void controlsAreClearedAndDefaulted_whenDisplayingNullSelection() {
        final Shell shell = new Shell(Display.getDefault());
        shell.open();

        final RobotKeywordFailBreakpointDetailPane pane = new RobotKeywordFailBreakpointDetailPane();

        pane.init(mock(IWorkbenchPartSite.class));
        pane.createControl(shell);

        final List<Control> controls = Controls.getControls(shell);
        final Button hitCountButton = (Button) controls.get(0);
        final Text hitCountText = (Text) controls.get(1);
        final Text patternText = (Text) controls.get(3);

        hitCountButton.setSelection(true);
        hitCountButton.notifyListeners(SWT.Selection, new Event());

        hitCountText.setText("42");
        hitCountText.notifyListeners(SWT.Modify, new Event());

        patternText.setText("Keyword Pattern");
        patternText.notifyListeners(SWT.Modify, new Event());

        pane.display((IStructuredSelection) null);

        assertThat(hitCountButton.getSelection()).isFalse();
        assertThat(hitCountText.getText()).isEmpty();
        assertThat(patternText.getText()).isEmpty();

        shell.close();
        shell.dispose();
    }

    @Test
    public void controlsAreClearedAndDefaulted_whenDisplayingSelectionContainingMoreThanOneBreakpoint() {
        final Shell shell = new Shell(Display.getDefault());
        shell.open();

        final RobotKeywordFailBreakpointDetailPane pane = new RobotKeywordFailBreakpointDetailPane();

        pane.init(mock(IWorkbenchPartSite.class));
        pane.createControl(shell);

        final List<Control> controls = Controls.getControls(shell);
        final Button hitCountButton = (Button) controls.get(0);
        final Text hitCountText = (Text) controls.get(1);
        final Text patternText = (Text) controls.get(3);

        hitCountButton.setSelection(true);
        hitCountButton.notifyListeners(SWT.Selection, new Event());

        hitCountText.setText("42");
        hitCountText.notifyListeners(SWT.Modify, new Event());

        patternText.setText("Keyword Pattern");
        patternText.notifyListeners(SWT.Modify, new Event());

        pane.display(new StructuredSelection(
                new Object[] { mock(RobotKeywordFailBreakpoint.class), mock(RobotKeywordFailBreakpoint.class) }));

        assertThat(hitCountButton.getSelection()).isFalse();
        assertThat(hitCountText.getText()).isEmpty();
        assertThat(patternText.getText()).isEmpty();

        shell.close();
        shell.dispose();
    }

    @Test
    public void controlsAreFilledWithDataTakenFromBreakpoint_whenDisplayingSelectionWithSingleBreakpoint() {
        final Shell shell = new Shell(Display.getDefault());
        shell.open();

        final RobotKeywordFailBreakpointDetailPane pane = new RobotKeywordFailBreakpointDetailPane();

        pane.init(mock(IWorkbenchPartSite.class));
        pane.createControl(shell);

        final List<Control> controls = Controls.getControls(shell);
        final Button hitCountButton = (Button) controls.get(0);
        final Text hitCountText = (Text) controls.get(1);
        final Text patternText = (Text) controls.get(3);

        hitCountButton.setSelection(true);
        hitCountButton.notifyListeners(SWT.Selection, new Event());

        hitCountText.setText("42");
        hitCountText.notifyListeners(SWT.Modify, new Event());

        patternText.setText("Keyword Pattern");
        patternText.notifyListeners(SWT.Modify, new Event());

        final RobotKeywordFailBreakpoint breakpoint = mock(RobotKeywordFailBreakpoint.class);
        when(breakpoint.isHitCountEnabled()).thenReturn(true);
        when(breakpoint.getHitCount()).thenReturn(1729);
        when(breakpoint.getNamePattern()).thenReturn("Other Pattern");
        when(breakpoint.getMarker()).thenReturn(mock(IMarker.class));
        pane.display(new StructuredSelection(new Object[] { breakpoint }));

        assertThat(hitCountButton.getSelection()).isTrue();
        assertThat(hitCountText.getText()).isEqualTo("1729");
        assertThat(patternText.getText()).isEqualTo("Other Pattern");

        shell.close();
        shell.dispose();
    }

    @Test
    public void stateFromTheControlsAreProperlySavedIntoCurrentBreakpoint() throws Exception {
        final Shell shell = new Shell(Display.getDefault());
        shell.open();

        final RobotKeywordFailBreakpointDetailPane pane = new RobotKeywordFailBreakpointDetailPane();

        pane.init(mock(IWorkbenchPartSite.class));
        pane.createControl(shell);

        final List<Control> controls = Controls.getControls(shell);
        final Button hitCountButton = (Button) controls.get(0);
        final Text hitCountText = (Text) controls.get(1);
        final Text patternText = (Text) controls.get(3);

        final IMarker marker = mock(IMarker.class);
        when(marker.exists()).thenReturn(true);
        final RobotKeywordFailBreakpoint breakpoint = mock(RobotKeywordFailBreakpoint.class);
        when(breakpoint.isHitCountEnabled()).thenReturn(true);
        when(breakpoint.getHitCount()).thenReturn(1729);
        when(breakpoint.getNamePattern()).thenReturn("Other Pattern");
        when(breakpoint.getMarker()).thenReturn(marker);
        pane.display(new StructuredSelection(new Object[] { breakpoint }));

        hitCountButton.setSelection(true);
        hitCountButton.notifyListeners(SWT.Selection, new Event());

        hitCountText.setText("42");
        hitCountText.notifyListeners(SWT.Modify, new Event());

        patternText.setText("The Pattern");
        patternText.notifyListeners(SWT.Modify, new Event());

        pane.doSave(null);
        assertThat(pane.isDirty()).isFalse();

        verify(breakpoint).setHitCountEnabled(true);
        verify(breakpoint).setHitCount(42);
        verify(breakpoint).setNamePattern("The Pattern");

        shell.close();
        shell.dispose();
    }
}
