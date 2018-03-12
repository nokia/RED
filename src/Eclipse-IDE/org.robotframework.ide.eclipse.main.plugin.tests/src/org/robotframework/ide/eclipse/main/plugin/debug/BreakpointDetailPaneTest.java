/*
* Copyright 2017 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.robotframework.ide.eclipse.main.plugin.debug;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.ISaveablePart;
import org.eclipse.ui.IWorkbenchPartSite;
import org.junit.Test;
import org.robotframework.ide.eclipse.main.plugin.debug.model.RobotLineBreakpoint;

public class BreakpointDetailPaneTest {

    @Test
    public void panePropertiesTest() {
        final BreakpointDetailPane pane = new BreakpointDetailPane();

        assertThat(pane.getID()).isEqualTo(BreakpointDetailPane.ID);
        assertThat(pane.getName()).isEqualTo(BreakpointDetailPane.NAME);
        assertThat(pane.getDescription()).isEqualTo(BreakpointDetailPane.DESCRIPTION);
        assertThat(pane.isDirty()).isFalse();
    }

    @Test
    public void propertyListenersAreNotified_whenPaneChangesDirtyFlag() {
        final BreakpointDetailPane pane = new BreakpointDetailPane();

        final IPropertyListener listener1 = mock(IPropertyListener.class);
        final IPropertyListener listener2 = mock(IPropertyListener.class);

        pane.addPropertyListener(listener1);
        pane.setDirty(true);

        assertThat(pane.isDirty()).isTrue();

        pane.addPropertyListener(listener2);
        pane.setDirty(false);
        assertThat(pane.isDirty()).isFalse();

        pane.removePropertyListener(listener1);
        pane.setDirty(true);
        assertThat(pane.isDirty()).isTrue();

        // dirty flag is cleared when initialized
        pane.init(mock(IWorkbenchPartSite.class));
        assertThat(pane.isDirty()).isFalse();
        pane.setDirty(true);

        verify(listener1, times(2)).propertyChanged(pane, ISaveablePart.PROP_DIRTY);
        verify(listener2, times(2)).propertyChanged(pane, ISaveablePart.PROP_DIRTY);
        verifyNoMoreInteractions(listener1, listener2);
    }

    @Test
    public void dirtyFlagIsClearedAndListenersAreRemoved_whenPaneIsInitialized() {
        final BreakpointDetailPane pane = new BreakpointDetailPane();
        pane.setDirty(true);

        final IPropertyListener listener = mock(IPropertyListener.class);
        pane.addPropertyListener(listener);
        pane.init(mock(IWorkbenchPartSite.class));

        assertThat(pane.isDirty()).isFalse();
        pane.setDirty(true);

        verifyZeroInteractions(listener);
    }

    @Test
    public void listenersAreRemoved_whenPaneIsDisposed() {
        final BreakpointDetailPane pane = new BreakpointDetailPane();

        final IPropertyListener listener = mock(IPropertyListener.class);
        pane.addPropertyListener(listener);
        pane.dispose();
        pane.setDirty(true);

        verifyZeroInteractions(listener);
    }

    @Test
    public void saveOnCloseIsNotNeededAndSaveAsIsNotAllowed() {
        final BreakpointDetailPane pane = spy(new BreakpointDetailPane());

        assertThat(pane.isSaveOnCloseNeeded()).isFalse();
        assertThat(pane.isSaveAsAllowed()).isFalse();
        pane.doSaveAs();

        verify(pane).isSaveOnCloseNeeded();
        verify(pane).isSaveAsAllowed();
        verify(pane).doSaveAs();
        verifyNoMoreInteractions(pane);
    }

    @Test
    public void checkControlsStateAfterCreation() {
        final Shell shell = new Shell(Display.getDefault());
        shell.open();

        final IDialogSettings paneSection = mock(IDialogSettings.class);
        when(paneSection.getArray(BreakpointDetailPane.OLD_CONDITIONS_ID))
                .thenReturn(new String[] { "old_condition", "old_condition  arg1  arg2" });

        final IDialogSettings dialogSettings = mock(IDialogSettings.class);
        when(dialogSettings.getSection(BreakpointDetailPane.ID)).thenReturn(paneSection);

        final BreakpointDetailPane pane = new BreakpointDetailPane(dialogSettings);

        pane.init(mock(IWorkbenchPartSite.class));
        pane.createControl(shell);

        assertThat(pane.setFocus()).isFalse();

        final List<Control> controls = getControls(shell);
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

        final BreakpointDetailPane pane = new BreakpointDetailPane(mock(IDialogSettings.class));

        pane.init(mock(IWorkbenchPartSite.class));
        pane.createControl(shell);

        final Button hitCountButton = (Button) getControls(shell).get(0);
        final Text hitCountText = (Text) getControls(shell).get(1);

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

        final BreakpointDetailPane pane = new BreakpointDetailPane(mock(IDialogSettings.class));

        pane.init(mock(IWorkbenchPartSite.class));
        pane.createControl(shell);

        final Button hitCountButton = (Button) getControls(shell).get(0);
        final Text hitCountText = (Text) getControls(shell).get(1);

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

        final BreakpointDetailPane pane = new BreakpointDetailPane(mock(IDialogSettings.class));

        pane.init(mock(IWorkbenchPartSite.class));
        pane.createControl(shell);

        final Button conditionButton = (Button) getControls(shell).get(2);
        final Combo conditionCombo = (Combo) getControls(shell).get(3);

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

        final BreakpointDetailPane pane = new BreakpointDetailPane(mock(IDialogSettings.class));

        pane.init(mock(IWorkbenchPartSite.class));
        pane.createControl(shell);

        final Button conditionButton = (Button) getControls(shell).get(2);
        final Combo conditionCombo = (Combo) getControls(shell).get(3);

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
        when(conditionsSection.getArray(BreakpointDetailPane.OLD_CONDITIONS_ID)).thenReturn(new String[0]);

        final IDialogSettings settings = conditionsSection;
        when(settings.getSection(BreakpointDetailPane.ID)).thenReturn(conditionsSection);
        final BreakpointDetailPane pane = new BreakpointDetailPane(settings);

        pane.init(mock(IWorkbenchPartSite.class));
        pane.createControl(shell);

        final Button hitCountButton = (Button) getControls(shell).get(0);
        final Text hitCountText = (Text) getControls(shell).get(1);
        final Button conditionButton = (Button) getControls(shell).get(2);
        final Combo conditionCombo = (Combo) getControls(shell).get(3);

        hitCountButton.setSelection(true);
        hitCountButton.notifyListeners(SWT.Selection, new Event());
        conditionButton.setSelection(true);
        conditionButton.notifyListeners(SWT.Selection, new Event());

        hitCountText.setText("42");
        hitCountText.notifyListeners(SWT.Modify, new Event());
        conditionCombo.setText("condition");
        conditionCombo.notifyListeners(SWT.Modify, new Event());

        pane.display(null);

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
        when(conditionsSection.getArray(BreakpointDetailPane.OLD_CONDITIONS_ID)).thenReturn(new String[0]);

        final IDialogSettings settings = conditionsSection;
        when(settings.getSection(BreakpointDetailPane.ID)).thenReturn(conditionsSection);
        final BreakpointDetailPane pane = new BreakpointDetailPane(settings);

        pane.init(mock(IWorkbenchPartSite.class));
        pane.createControl(shell);

        final Button hitCountButton = (Button) getControls(shell).get(0);
        final Text hitCountText = (Text) getControls(shell).get(1);
        final Button conditionButton = (Button) getControls(shell).get(2);
        final Combo conditionCombo = (Combo) getControls(shell).get(3);

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
        when(conditionsSection.getArray(BreakpointDetailPane.OLD_CONDITIONS_ID)).thenReturn(new String[0]);

        final IDialogSettings settings = conditionsSection;
        when(settings.getSection(BreakpointDetailPane.ID)).thenReturn(conditionsSection);
        final BreakpointDetailPane pane = new BreakpointDetailPane(settings);

        pane.init(mock(IWorkbenchPartSite.class));
        pane.createControl(shell);

        final Button hitCountButton = (Button) getControls(shell).get(0);
        final Text hitCountText = (Text) getControls(shell).get(1);
        final Button conditionButton = (Button) getControls(shell).get(2);
        final Combo conditionCombo = (Combo) getControls(shell).get(3);

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
        when(breakpoint.getCondition()).thenReturn("bp condition");
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
        when(conditionsSection.getArray(BreakpointDetailPane.OLD_CONDITIONS_ID)).thenReturn(new String[0]);

        final IDialogSettings settings = conditionsSection;
        when(settings.getSection(BreakpointDetailPane.ID)).thenReturn(conditionsSection);
        final BreakpointDetailPane pane = new BreakpointDetailPane(settings);

        pane.init(mock(IWorkbenchPartSite.class));
        pane.createControl(shell);

        final Button hitCountButton = (Button) getControls(shell).get(0);
        final Text hitCountText = (Text) getControls(shell).get(1);
        final Button conditionButton = (Button) getControls(shell).get(2);
        final Combo conditionCombo = (Combo) getControls(shell).get(3);

        final IMarker marker = mock(IMarker.class);
        when(marker.exists()).thenReturn(true);
        final RobotLineBreakpoint breakpoint = mock(RobotLineBreakpoint.class);
        when(breakpoint.isHitCountEnabled()).thenReturn(false);
        when(breakpoint.isConditionEnabled()).thenReturn(false);
        when(breakpoint.getHitCount()).thenReturn(0);
        when(breakpoint.getCondition()).thenReturn("");
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
