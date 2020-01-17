/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.debug;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.ISaveablePart;
import org.eclipse.ui.IWorkbenchPartSite;
import org.junit.jupiter.api.Test;
import org.rf.ide.core.execution.debug.RobotBreakpoint;

public class RobotBreakpointDetailPaneTest {

    @Test
    public void propertyListenersAreNotified_whenPaneChangesDirtyFlag() {
        final RobotBreakpointDetailPaneImpl pane = new RobotBreakpointDetailPaneImpl();

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
        final RobotBreakpointDetailPaneImpl pane = new RobotBreakpointDetailPaneImpl();
        pane.setDirty(true);

        final IPropertyListener listener = mock(IPropertyListener.class);
        pane.addPropertyListener(listener);
        pane.init(mock(IWorkbenchPartSite.class));

        assertThat(pane.isDirty()).isFalse();
        pane.setDirty(true);

        verifyNoInteractions(listener);
    }

    @Test
    public void listenersAreRemoved_whenPaneIsDisposed() {
        final RobotBreakpointDetailPaneImpl pane = new RobotBreakpointDetailPaneImpl();

        final IPropertyListener listener = mock(IPropertyListener.class);
        pane.addPropertyListener(listener);
        pane.dispose();
        pane.setDirty(true);

        verifyNoInteractions(listener);
    }

    @Test
    public void saveOnCloseIsNotNeededAndSaveAsIsNotAllowed() {
        final RobotBreakpointDetailPaneImpl pane = spy(new RobotBreakpointDetailPaneImpl());

        assertThat(pane.isSaveOnCloseNeeded()).isFalse();
        assertThat(pane.isSaveAsAllowed()).isFalse();
        pane.doSaveAs();

        verify(pane).isSaveOnCloseNeeded();
        verify(pane).isSaveAsAllowed();
        verify(pane).doSaveAs();
        verifyNoMoreInteractions(pane);
    }

    public static class RobotBreakpointDetailPaneImpl extends RobotBreakpointDetailPane {

        @Override
        public String getID() {
            return "id";
        }

        @Override
        public String getName() {
            return "name";
        }

        @Override
        public String getDescription() {
            return "desc";
        }

        @Override
        protected void createSpecificControls(final Composite panel) {
            // nothing to create
        }

        @Override
        protected Class<? extends IBreakpoint> getBreakpointClass() {
            return IBreakpoint.class;
        }

        @Override
        protected void doSaveSpecificAttributes(final RobotBreakpoint currentBreakpoint) throws CoreException {
            // nothing to save
        }
    }
}
