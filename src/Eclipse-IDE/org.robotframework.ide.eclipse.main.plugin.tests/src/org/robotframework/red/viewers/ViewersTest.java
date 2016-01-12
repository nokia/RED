/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.red.viewers;

import static org.assertj.core.api.Assertions.assertThat;
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
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.contexts.IContextActivation;
import org.eclipse.ui.contexts.IContextService;
import org.junit.Rule;
import org.junit.Test;
import org.robotframework.red.junit.ShellProvider;

public class ViewersTest {

    @Rule
    public ShellProvider shellProvider = new ShellProvider();

    @Test
    public void contextIsActivated_whenViewerWithBoundedContextGetsFocused() {
        final IContextService contextService = mock(IContextService.class);

        final IWorkbenchSite site = mock(IWorkbenchSite.class);
        when(site.getService(IContextService.class)).thenReturn(contextService);

        final TableViewer viewer = new TableViewer(shellProvider.getShell());
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

        final ColumnViewer viewer = new TreeViewer(shellProvider.getShell());
        final Label label = new Label(shellProvider.getShell(), SWT.NONE);

        Viewers.boundViewerWithContext(viewer, site, "contextId");

        viewer.getControl().notifyListeners(SWT.FocusIn, new Event());
        viewer.getControl().notifyListeners(SWT.FocusOut, new Event());
        label.notifyListeners(SWT.FocusIn, new Event());

        verify(contextService, times(1)).activateContext("contextId");
        verify(contextService, times(1)).deactivateContext(activationToken);
    }

    @Test
    public void creationIndexIsReturnedFromPosition_forViewerWithSwappedColumns() {
        final TreeViewer viewer = new TreeViewer(shellProvider.getShell());

        new TreeViewerColumn(viewer, SWT.NONE);
        new TreeViewerColumn(viewer, SWT.NONE);
        new TreeViewerColumn(viewer, SWT.NONE);

        // swapping order
        viewer.getTree().setColumnOrder(new int[] { 1, 2, 0 });

        assertThat(Viewers.createOrderIndexToPositionIndex(viewer, 0)).isEqualTo(2);
        assertThat(Viewers.createOrderIndexToPositionIndex(viewer, 1)).isEqualTo(0);
        assertThat(Viewers.createOrderIndexToPositionIndex(viewer, 2)).isEqualTo(1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void exceptionIsThrown_whenGettingPositionFromNonExistingOrderIndex() {
        final TreeViewer viewer = new TreeViewer(shellProvider.getShell());

        new TreeViewerColumn(viewer, SWT.NONE);
        new TreeViewerColumn(viewer, SWT.NONE);

        // swapping order
        viewer.getTree().setColumnOrder(new int[] { 1, 0 });

        Viewers.createOrderIndexToPositionIndex(viewer, 2);
    }

    @Test(expected = IllegalStateException.class)
    public void exceptionIsThrown_whenTryingToGetPositionFromNullViewer() {
        Viewers.createOrderIndexToPositionIndex(null, 2);
    }

    @Test(expected = IllegalStateException.class)
    public void exceptionIsThrown_whenTryingToGetPositionFromUnknownViewerType() {
        final ColumnViewer viewer = mock(ColumnViewer.class);
        Viewers.createOrderIndexToPositionIndex(viewer, 2);
    }

    @Test
    public void positionIsReturnedFromCreationIndex_forViewerWithSwappedColumns() {
        final TableViewer viewer = new TableViewer(shellProvider.getShell());

        new TableViewerColumn(viewer, SWT.NONE);
        new TableViewerColumn(viewer, SWT.NONE);
        new TableViewerColumn(viewer, SWT.NONE);

        // swapping order
        viewer.getTable().setColumnOrder(new int[] { 1, 2, 0 });

        assertThat(Viewers.positionIndexToCreateOrderIndex(viewer, 0)).isEqualTo(1);
        assertThat(Viewers.positionIndexToCreateOrderIndex(viewer, 1)).isEqualTo(2);
        assertThat(Viewers.positionIndexToCreateOrderIndex(viewer, 2)).isEqualTo(0);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void exceptionIsThrown_whenGettingOrderIndexFromNonExistingPosition() {
        final TableViewer viewer = new TableViewer(shellProvider.getShell());

        new TableViewerColumn(viewer, SWT.NONE);
        new TableViewerColumn(viewer, SWT.NONE);

        // swapping order
        viewer.getTable().setColumnOrder(new int[] { 1, 0 });

        Viewers.positionIndexToCreateOrderIndex(viewer, 2);
    }

    @Test(expected = IllegalStateException.class)
    public void exceptionIsThrown_whenTryingToGetCreateIndexFromNullViewer() {
        Viewers.positionIndexToCreateOrderIndex(null, 2);
    }

    @Test(expected = IllegalStateException.class)
    public void exceptionIsThrown_whenTryingToGetCreateIndexFromUnknownViewerType() {
        final ColumnViewer viewer = mock(ColumnViewer.class);
        Viewers.positionIndexToCreateOrderIndex(viewer, 2);
    }
}
