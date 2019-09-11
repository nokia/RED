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
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchPartSite;
import org.junit.Test;
import org.robotframework.ide.eclipse.main.plugin.debug.model.RobotLineBreakpoint;
import org.robotframework.red.junit.Controls;

public class RobotLineBreakpointDetailPaneTest {

    @Test
    public void panePropertiesTest() {
        final RobotLineBreakpointDetailPane pane = new RobotLineBreakpointDetailPane();

        assertThat(pane.getID()).isEqualTo(RobotLineBreakpointDetailPane.ID);
        assertThat(pane.getName()).isEqualTo(RobotLineBreakpointDetailPane.NAME);
        assertThat(pane.getDescription()).isEqualTo(RobotLineBreakpointDetailPane.DESCRIPTION);
        assertThat(pane.isDirty()).isFalse();
    }

    @Test
    public void checkControlsStateAfterCreation() {
        final Shell shell = new Shell(Display.getDefault());
        shell.open();

        final IDialogSettings paneSection = mock(IDialogSettings.class);
        when(paneSection.getArray(RobotLineBreakpointDetailPane.OLD_CONDITIONS_ID))
                .thenReturn(new String[] { "old_condition", "old_condition  arg1  arg2" });

        final IDialogSettings dialogSettings = mock(IDialogSettings.class);
        when(dialogSettings.getSection(RobotLineBreakpointDetailPane.ID)).thenReturn(paneSection);

        final RobotLineBreakpointDetailPane pane = new RobotLineBreakpointDetailPane(dialogSettings);

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

        assertThat(controls.get(2)).isInstanceOf(Button.class);
        assertThat(((Button) controls.get(2)).getStyle() & SWT.CHECK).isEqualTo(SWT.CHECK);
        assertThat(((Button) controls.get(2)).isEnabled()).isTrue();
        assertThat(((Button) controls.get(2)).getSelection()).isFalse();

        assertThat(controls.get(3)).isInstanceOf(Combo.class);
        assertThat(((Combo) controls.get(3)).isEnabled()).isFalse();
        assertThat(((Combo) controls.get(3)).getText()).isEmpty();
        assertThat(((Combo) controls.get(3)).getItems()).containsExactly("old_condition", "old_condition  arg1  arg2");

        shell.close();
        shell.dispose();
    }

    @Test
    public void checkingAndUncheckingHitCountButtonEnablesAndDisablesText_alsoPaneGetsDirty() {
        final Shell shell = new Shell(Display.getDefault());
        shell.open();

        final RobotLineBreakpointDetailPane pane = new RobotLineBreakpointDetailPane(mock(IDialogSettings.class));

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

        final RobotLineBreakpointDetailPane pane = new RobotLineBreakpointDetailPane(mock(IDialogSettings.class));

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
    public void checkingAndUncheckingConditionButtonEnablesAndDisablesCombo_alsoPaneGetsDirty() {
        final Shell shell = new Shell(Display.getDefault());
        shell.open();

        final RobotLineBreakpointDetailPane pane = new RobotLineBreakpointDetailPane(mock(IDialogSettings.class));

        pane.init(mock(IWorkbenchPartSite.class));
        pane.createControl(shell);

        final List<Control> controls = Controls.getControls(shell);
        final Button conditionButton = (Button) controls.get(2);
        final Combo conditionCombo = (Combo) controls.get(3);

        assertThat(pane.isDirty()).isFalse();
        assertThat(conditionCombo.isEnabled()).isFalse();

        conditionButton.setSelection(true);
        conditionButton.notifyListeners(SWT.Selection, new Event());

        assertThat(pane.isDirty()).isTrue();
        assertThat(conditionCombo.isEnabled()).isTrue();

        pane.setDirty(false);

        conditionButton.setSelection(false);
        conditionButton.notifyListeners(SWT.Selection, new Event());

        assertThat(pane.isDirty()).isTrue();
        assertThat(conditionCombo.isEnabled()).isFalse();

        shell.close();
        shell.dispose();
    }

    @Test
    public void modifyingConditionComboMakesPaneDirty() {
        final Shell shell = new Shell(Display.getDefault());
        shell.open();

        final RobotLineBreakpointDetailPane pane = new RobotLineBreakpointDetailPane(mock(IDialogSettings.class));

        pane.init(mock(IWorkbenchPartSite.class));
        pane.createControl(shell);

        final List<Control> controls = Controls.getControls(shell);
        final Button conditionButton = (Button) controls.get(2);
        final Combo conditionCombo = (Combo) controls.get(3);

        assertThat(pane.isDirty()).isFalse();

        conditionButton.setSelection(true);
        conditionButton.notifyListeners(SWT.Selection, new Event());

        conditionCombo.setText("condition");
        conditionCombo.notifyListeners(SWT.Modify, new Event());

        assertThat(pane.isDirty()).isTrue();

        shell.close();
        shell.dispose();
    }

    @Test
    public void controlsAreClearedAndDefaulted_whenDisplayingNullSelection() {
        final Shell shell = new Shell(Display.getDefault());
        shell.open();

        final IDialogSettings conditionsSection = mock(IDialogSettings.class);
        when(conditionsSection.getArray(RobotLineBreakpointDetailPane.OLD_CONDITIONS_ID)).thenReturn(new String[0]);

        final IDialogSettings settings = conditionsSection;
        when(settings.getSection(RobotLineBreakpointDetailPane.ID)).thenReturn(conditionsSection);
        final RobotLineBreakpointDetailPane pane = new RobotLineBreakpointDetailPane(settings);

        pane.init(mock(IWorkbenchPartSite.class));
        pane.createControl(shell);

        final List<Control> controls = Controls.getControls(shell);
        final Button hitCountButton = (Button) controls.get(0);
        final Text hitCountText = (Text) controls.get(1);
        final Button conditionButton = (Button) controls.get(2);
        final Combo conditionCombo = (Combo) controls.get(3);

        hitCountButton.setSelection(true);
        hitCountButton.notifyListeners(SWT.Selection, new Event());
        conditionButton.setSelection(true);
        conditionButton.notifyListeners(SWT.Selection, new Event());

        hitCountText.setText("42");
        hitCountText.notifyListeners(SWT.Modify, new Event());
        conditionCombo.setText("condition");
        conditionCombo.notifyListeners(SWT.Modify, new Event());

        pane.display((IStructuredSelection) null);

        assertThat(hitCountButton.getSelection()).isFalse();
        assertThat(conditionButton.getSelection()).isFalse();
        assertThat(hitCountText.getText()).isEmpty();
        assertThat(conditionCombo.getText()).isEmpty();

        shell.close();
        shell.dispose();
    }

    @Test
    public void controlsAreClearedAndDefaulted_whenDisplayingSelectionContainingMoreThanOneBreakpoint() {
        final Shell shell = new Shell(Display.getDefault());
        shell.open();

        final IDialogSettings conditionsSection = mock(IDialogSettings.class);
        when(conditionsSection.getArray(RobotLineBreakpointDetailPane.OLD_CONDITIONS_ID)).thenReturn(new String[0]);

        final IDialogSettings settings = conditionsSection;
        when(settings.getSection(RobotLineBreakpointDetailPane.ID)).thenReturn(conditionsSection);
        final RobotLineBreakpointDetailPane pane = new RobotLineBreakpointDetailPane(settings);

        pane.init(mock(IWorkbenchPartSite.class));
        pane.createControl(shell);

        final List<Control> controls = Controls.getControls(shell);
        final Button hitCountButton = (Button) controls.get(0);
        final Text hitCountText = (Text) controls.get(1);
        final Button conditionButton = (Button) controls.get(2);
        final Combo conditionCombo = (Combo) controls.get(3);

        hitCountButton.setSelection(true);
        hitCountButton.notifyListeners(SWT.Selection, new Event());
        conditionButton.setSelection(true);
        conditionButton.notifyListeners(SWT.Selection, new Event());

        hitCountText.setText("42");
        hitCountText.notifyListeners(SWT.Modify, new Event());
        conditionCombo.setText("condition");
        conditionCombo.notifyListeners(SWT.Modify, new Event());

        pane.display(new StructuredSelection(
                new Object[] { mock(RobotLineBreakpoint.class), mock(RobotLineBreakpoint.class) }));

        assertThat(hitCountButton.getSelection()).isFalse();
        assertThat(conditionButton.getSelection()).isFalse();
        assertThat(hitCountText.getText()).isEmpty();
        assertThat(conditionCombo.getText()).isEmpty();

        shell.close();
        shell.dispose();
    }

    @Test
    public void controlsAreFilledWithDataTakenFromBreakpoint_whenDisplayingSelectionWithSingleBreakpoint() {
        final Shell shell = new Shell(Display.getDefault());
        shell.open();

        final IDialogSettings conditionsSection = mock(IDialogSettings.class);
        when(conditionsSection.getArray(RobotLineBreakpointDetailPane.OLD_CONDITIONS_ID)).thenReturn(new String[0]);

        final IDialogSettings settings = conditionsSection;
        when(settings.getSection(RobotLineBreakpointDetailPane.ID)).thenReturn(conditionsSection);
        final RobotLineBreakpointDetailPane pane = new RobotLineBreakpointDetailPane(settings);

        pane.init(mock(IWorkbenchPartSite.class));
        pane.createControl(shell);

        final List<Control> controls = Controls.getControls(shell);
        final Button hitCountButton = (Button) controls.get(0);
        final Text hitCountText = (Text) controls.get(1);
        final Button conditionButton = (Button) controls.get(2);
        final Combo conditionCombo = (Combo) controls.get(3);

        hitCountButton.setSelection(true);
        hitCountButton.notifyListeners(SWT.Selection, new Event());
        conditionButton.setSelection(true);
        conditionButton.notifyListeners(SWT.Selection, new Event());

        hitCountText.setText("42");
        hitCountText.notifyListeners(SWT.Modify, new Event());
        conditionCombo.setText("condition");
        conditionCombo.notifyListeners(SWT.Modify, new Event());

        final RobotLineBreakpoint breakpoint = mock(RobotLineBreakpoint.class);
        when(breakpoint.isHitCountEnabled()).thenReturn(true);
        when(breakpoint.isConditionEnabled()).thenReturn(true);
        when(breakpoint.getHitCount()).thenReturn(1729);
        when(breakpoint.getConditionExpression()).thenReturn("bp condition");
        when(breakpoint.getMarker()).thenReturn(mock(IMarker.class));
        pane.display(new StructuredSelection(new Object[] { breakpoint }));

        assertThat(hitCountButton.getSelection()).isTrue();
        assertThat(conditionButton.getSelection()).isTrue();
        assertThat(hitCountText.getText()).isEqualTo("1729");
        assertThat(conditionCombo.getText()).isEqualTo("bp condition");

        shell.close();
        shell.dispose();
    }

    @Test
    public void stateFromTheControlsAreProperlySavedIntoCurrentBreakpoint() throws Exception {
        final Shell shell = new Shell(Display.getDefault());
        shell.open();

        final IDialogSettings conditionsSection = mock(IDialogSettings.class);
        when(conditionsSection.getArray(RobotLineBreakpointDetailPane.OLD_CONDITIONS_ID)).thenReturn(new String[0]);

        final IDialogSettings settings = conditionsSection;
        when(settings.getSection(RobotLineBreakpointDetailPane.ID)).thenReturn(conditionsSection);
        final RobotLineBreakpointDetailPane pane = new RobotLineBreakpointDetailPane(settings);

        pane.init(mock(IWorkbenchPartSite.class));
        pane.createControl(shell);

        final List<Control> controls = Controls.getControls(shell);
        final Button hitCountButton = (Button) controls.get(0);
        final Text hitCountText = (Text) controls.get(1);
        final Button conditionButton = (Button) controls.get(2);
        final Combo conditionCombo = (Combo) controls.get(3);

        final IMarker marker = mock(IMarker.class);
        when(marker.exists()).thenReturn(true);
        final RobotLineBreakpoint breakpoint = mock(RobotLineBreakpoint.class);
        when(breakpoint.isHitCountEnabled()).thenReturn(false);
        when(breakpoint.isConditionEnabled()).thenReturn(false);
        when(breakpoint.getHitCount()).thenReturn(0);
        when(breakpoint.getConditionExpression()).thenReturn("");
        when(breakpoint.getMarker()).thenReturn(marker);
        pane.display(new StructuredSelection(new Object[] { breakpoint }));

        hitCountButton.setSelection(true);
        hitCountButton.notifyListeners(SWT.Selection, new Event());
        conditionButton.setSelection(true);
        conditionButton.notifyListeners(SWT.Selection, new Event());

        hitCountText.setText("42");
        hitCountText.notifyListeners(SWT.Modify, new Event());
        conditionCombo.setText("condition");
        conditionCombo.notifyListeners(SWT.Modify, new Event());

        pane.doSave(null);
        assertThat(pane.isDirty()).isFalse();

        verify(breakpoint).setHitCountEnabled(true);
        verify(breakpoint).setHitCount(42);
        verify(breakpoint).setConditionEnabled(true);
        verify(breakpoint).setCondition("condition");

        shell.close();
        shell.dispose();
    }
}
