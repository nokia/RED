/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.red.viewers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.contexts.IContextActivation;
import org.eclipse.ui.contexts.IContextService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.robotframework.red.junit.jupiter.FreshShell;
import org.robotframework.red.junit.jupiter.FreshShellExtension;

@ExtendWith(FreshShellExtension.class)
public class ViewersTest {

    @FreshShell
    Shell shell;

    @Test
    public void contextIsActivated_whenViewerWithBoundedContextGetsFocused() {
        final IContextService contextService = mock(IContextService.class);

        final IWorkbenchSite site = mock(IWorkbenchSite.class);
        when(site.getService(IContextService.class)).thenReturn(contextService);

        final TableViewer viewer = new TableViewer(shell);
        Viewers.boundViewerWithContext(viewer, site, "contextId");

        viewer.getControl().notifyListeners(SWT.FocusIn, new Event());

        verify(contextService, times(1)).activateContext("contextId");
    }

    @Test
    public void contextIsDeactivated_whenFocusIsLostToOtherControl() {
        final IContextActivation activationToken = mock(IContextActivation.class);

        final IContextService contextService = mock(IContextService.class);
        when(contextService.activateContext("contextId")).thenReturn(activationToken);

        final IWorkbenchSite site = mock(IWorkbenchSite.class);
        when(site.getService(IContextService.class)).thenReturn(contextService).thenReturn(contextService);

        final ColumnViewer viewer = new TreeViewer(shell);
        final Label label = new Label(shell, SWT.NONE);

        Viewers.boundViewerWithContext(viewer, site, "contextId");

        viewer.getControl().notifyListeners(SWT.FocusIn, new Event());
        viewer.getControl().notifyListeners(SWT.FocusOut, new Event());
        label.notifyListeners(SWT.FocusIn, new Event());

        verify(contextService, times(1)).activateContext("contextId");
        verify(contextService, times(1)).deactivateContext(activationToken);
    }

    @Test
    public void creationIndexIsReturnedFromPosition_forViewerWithSwappedColumns() {
        final TreeViewer viewer = new TreeViewer(shell);

        new TreeViewerColumn(viewer, SWT.NONE);
        new TreeViewerColumn(viewer, SWT.NONE);
        new TreeViewerColumn(viewer, SWT.NONE);

        // swapping order
        viewer.getTree().setColumnOrder(new int[] { 1, 2, 0 });

        assertThat(Viewers.createOrderIndexToPositionIndex(viewer, 0)).isEqualTo(2);
        assertThat(Viewers.createOrderIndexToPositionIndex(viewer, 1)).isEqualTo(0);
        assertThat(Viewers.createOrderIndexToPositionIndex(viewer, 2)).isEqualTo(1);
    }

    @Test
    public void exceptionIsThrown_whenGettingPositionFromNonExistingOrderIndex() {
        final TreeViewer viewer = new TreeViewer(shell);

        new TreeViewerColumn(viewer, SWT.NONE);
        new TreeViewerColumn(viewer, SWT.NONE);

        // swapping order
        viewer.getTree().setColumnOrder(new int[] { 1, 0 });

        assertThatIllegalArgumentException().isThrownBy(() -> Viewers.createOrderIndexToPositionIndex(viewer, 2));
    }

    @Test
    public void exceptionIsThrown_whenTryingToGetPositionFromNullViewer() {
        assertThatIllegalStateException().isThrownBy(() -> Viewers.createOrderIndexToPositionIndex(null, 2));
    }

    @Test
    public void exceptionIsThrown_whenTryingToGetPositionFromUnknownViewerType() {
        final ColumnViewer viewer = mock(ColumnViewer.class);
        assertThatIllegalStateException().isThrownBy(() -> Viewers.createOrderIndexToPositionIndex(viewer, 2));
    }

    @Test
    public void positionIsReturnedFromCreationIndex_forViewerWithSwappedColumns() {
        final TableViewer viewer = new TableViewer(shell);

        new TableViewerColumn(viewer, SWT.NONE);
        new TableViewerColumn(viewer, SWT.NONE);
        new TableViewerColumn(viewer, SWT.NONE);

        // swapping order
        viewer.getTable().setColumnOrder(new int[] { 1, 2, 0 });

        assertThat(Viewers.positionIndexToCreateOrderIndex(viewer, 0)).isEqualTo(1);
        assertThat(Viewers.positionIndexToCreateOrderIndex(viewer, 1)).isEqualTo(2);
        assertThat(Viewers.positionIndexToCreateOrderIndex(viewer, 2)).isEqualTo(0);
    }

    @Test
    public void exceptionIsThrown_whenGettingOrderIndexFromNonExistingPosition() {
        final TableViewer viewer = new TableViewer(shell);

        new TableViewerColumn(viewer, SWT.NONE);
        new TableViewerColumn(viewer, SWT.NONE);

        // swapping order
        viewer.getTable().setColumnOrder(new int[] { 1, 0 });

        assertThatExceptionOfType(IndexOutOfBoundsException.class)
                .isThrownBy(() -> Viewers.positionIndexToCreateOrderIndex(viewer, 2));
    }

    @Test
    public void exceptionIsThrown_whenTryingToGetCreateIndexFromNullViewer() {
        assertThatIllegalStateException().isThrownBy(() -> Viewers.positionIndexToCreateOrderIndex(null, 2));
    }

    @Test
    public void exceptionIsThrown_whenTryingToGetCreateIndexFromUnknownViewerType() {
        final ColumnViewer viewer = mock(ColumnViewer.class);
        assertThatIllegalStateException().isThrownBy(() -> Viewers.positionIndexToCreateOrderIndex(viewer, 2));
    }
}
