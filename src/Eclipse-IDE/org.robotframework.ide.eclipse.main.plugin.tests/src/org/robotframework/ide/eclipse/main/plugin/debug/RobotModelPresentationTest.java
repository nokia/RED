/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.debug;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.ILineBreakpoint;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.ui.IValueDetailListener;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.junit.Test;
import org.robotframework.ide.eclipse.main.plugin.debug.model.RobotDebugTarget;
import org.robotframework.ide.eclipse.main.plugin.debug.model.RobotDebugValueOfDictionary;
import org.robotframework.ide.eclipse.main.plugin.debug.model.RobotDebugValueOfList;
import org.robotframework.ide.eclipse.main.plugin.debug.model.RobotDebugValueOfScalar;
import org.robotframework.ide.eclipse.main.plugin.debug.model.RobotDebugVariable;
import org.robotframework.ide.eclipse.main.plugin.debug.model.RobotLineBreakpoint;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotFormEditor;

public class RobotModelPresentationTest {

    @Test
    public void setAttributeDoesNothing() {
        final RobotModelPresentation presentation = spy(new RobotModelPresentation());
        final Object someObject = mock(Object.class);

        presentation.setAttribute("attr", someObject);

        verifyZeroInteractions(someObject);
        verify(presentation).setAttribute("attr", someObject);
        verifyNoMoreInteractions(presentation);
    }

    @Test
    public void imageIsAlwaysNull() {
        final RobotModelPresentation presentation = new RobotModelPresentation();
        final Object someObject = mock(Object.class);

        assertThat(presentation.getImage(someObject)).isNull();
        verifyZeroInteractions(someObject);
    }

    @Test
    public void threadNameIsProvided_whenThreadIsGiven() throws CoreException {
        final RobotModelPresentation presentation = new RobotModelPresentation();

        final IThread thread = mock(IThread.class);
        when(thread.getName()).thenReturn("robot thread");

        assertThat(presentation.getText(thread)).isEqualTo("robot thread");
    }

    @Test
    public void debugTargetNameIsProvided_whenDebugTargetIsGiven() throws CoreException {
        final RobotModelPresentation presentation = new RobotModelPresentation();

        final IDebugTarget target = mock(IDebugTarget.class);
        when(target.getName()).thenReturn("target");

        assertThat(presentation.getText(target)).isEqualTo("target");
    }

    @Test
    public void stackframeNameIsProvided_whenStackframeIsGiven() throws CoreException {
        final RobotModelPresentation presentation = new RobotModelPresentation();

        final IStackFrame frame = mock(IStackFrame.class);
        when(frame.getName()).thenReturn("frame");

        assertThat(presentation.getText(frame)).isEqualTo("frame");
    }

    @Test
    public void breakpointLabelIsProvided_whenRobotLineBreakpointIsGiven() throws CoreException {
        final RobotModelPresentation presentation = new RobotModelPresentation();

        final RobotLineBreakpoint breakpoint = mock(RobotLineBreakpoint.class);
        when(breakpoint.getLabel()).thenReturn("breakpoint [line: 3]");

        assertThat(presentation.getText(breakpoint)).isEqualTo("breakpoint [line: 3]");
    }

    @Test
    public void redNameIsProvided_whenArbitraryObjectIsGiven() throws CoreException {
        final RobotModelPresentation presentation = new RobotModelPresentation();

        assertThat(presentation.getText(new Object())).isEqualTo("RED");
    }

    @Test
    public void robotSuiteEditorIdIsProvided_whenFileIsGiven() {
        final RobotModelPresentation presentation = new RobotModelPresentation();

        final IFile file = mock(IFile.class);
        final String id = presentation.getEditorId(mock(IEditorInput.class), file);

        assertThat(id).isEqualTo(RobotFormEditor.ID);
    }

    @Test
    public void fileEditorInputIsProvided_whenFileIsGiven() {
        final RobotModelPresentation presentation = new RobotModelPresentation();

        final IFile file = mock(IFile.class);
        final IEditorInput input = presentation.getEditorInput(file);

        assertThat(input).isInstanceOf(IFileEditorInput.class);
        assertThat(((IFileEditorInput) input).getFile()).isSameAs(file);
    }

    @Test
    public void robotSuiteEditorIdIsProvided_whenLineBreakpointIsGiven() {
        final RobotModelPresentation presentation = new RobotModelPresentation();

        final ILineBreakpoint breakpoint = mock(ILineBreakpoint.class);
        final String id = presentation.getEditorId(mock(IEditorInput.class), breakpoint);

        assertThat(id).isEqualTo(RobotFormEditor.ID);
    }

    @Test
    public void fileEditorInputIsProvided_whenLineBreakpointIsGiven() {
        final RobotModelPresentation presentation = new RobotModelPresentation();

        final IFile file = mock(IFile.class);
        final IMarker marker = mock(IMarker.class);
        when(marker.getResource()).thenReturn(file);

        final ILineBreakpoint breakpoint = mock(ILineBreakpoint.class);
        when(breakpoint.getMarker()).thenReturn(marker);

        final IEditorInput input = presentation.getEditorInput(breakpoint);

        assertThat(input).isInstanceOf(IFileEditorInput.class);
        assertThat(((IFileEditorInput) input).getFile()).isSameAs(file);
    }

    @Test
    public void nullEditorIdIsProvided_whenArbitraryObjectIsGiven() {
        final RobotModelPresentation presentation = new RobotModelPresentation();

        final String id = presentation.getEditorId(mock(IEditorInput.class), new Object());
        assertThat(id).isNull();
    }

    @Test
    public void nullEditorInputIsProvided_whenArbitraryObjectIsGiven() {
        final RobotModelPresentation presentation = new RobotModelPresentation();

        final IEditorInput input = presentation.getEditorInput(new Object());
        assertThat(input).isNull();
    }

    @Test
    public void whenRobotDebugValueIsComputed_listenerIsNotifiedAboutIt() {
        final RobotModelPresentation presentation = new RobotModelPresentation();

        final IValueDetailListener listener = mock(IValueDetailListener.class);

        final IValue value1 = new RobotDebugValueOfScalar(null, "val");
        final IValue value2 = new RobotDebugValueOfList(null, newArrayList(
                new RobotDebugVariable((RobotDebugTarget) null, "1", "x"),
                new RobotDebugVariable((RobotDebugTarget) null, "2", "y"),
                new RobotDebugVariable((RobotDebugTarget) null, "3", "z")));
        final IValue value3 = new RobotDebugValueOfDictionary(null, newArrayList(
                new RobotDebugVariable((RobotDebugTarget) null, "k1", "x"),
                new RobotDebugVariable((RobotDebugTarget) null, "k2", "y"),
                new RobotDebugVariable((RobotDebugTarget) null, "k3", "z")));

        presentation.computeDetail(value1, listener);
        presentation.computeDetail(value2, listener);
        presentation.computeDetail(value3, listener);

        verify(listener).detailComputed(value1, "val");
        verify(listener).detailComputed(value2, "[x, y, z]");
        verify(listener).detailComputed(value3, "{k1=x, k2=y, k3=z}");
    }
}
