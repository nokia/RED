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

import java.net.URI;
import java.util.ArrayList;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.ILineBreakpoint;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.ui.IValueDetailListener;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.viewers.DecorationOverlayIcon;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.junit.Test;
import org.rf.ide.core.execution.agent.event.VariableTypedValue;
import org.rf.ide.core.execution.debug.StackFrame;
import org.rf.ide.core.execution.debug.StackFrameVariable;
import org.rf.ide.core.testdata.model.table.variables.AVariable.VariableScope;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.debug.SourceInLibraryEditorInput.SourceOfStackFrameInLibrary;
import org.robotframework.ide.eclipse.main.plugin.debug.SourceNotFoundEditorInput.SourceOfStackFrameNotFound;
import org.robotframework.ide.eclipse.main.plugin.debug.model.RobotDebugVariable;
import org.robotframework.ide.eclipse.main.plugin.debug.model.RobotLineBreakpoint;
import org.robotframework.ide.eclipse.main.plugin.debug.model.RobotStackFrame;
import org.robotframework.ide.eclipse.main.plugin.debug.model.RobotThread;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotFormEditor;
import org.robotframework.red.graphics.ImagesManager;

import com.google.common.collect.ImmutableMap;

public class RobotModelPresentationTest {

    private final RobotModelPresentation presentation = new RobotModelPresentation();

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
    public void nullImageIsReturned_whenOrdinaryObjectIsGiven() {
        final Object someObject = mock(Object.class);

        assertThat(presentation.getImage(someObject)).isNull();
        verifyZeroInteractions(someObject);
    }

    @Test
    public void nonDecoratedStackFrameImageIsReturned_whenNormalStackFrameIsGiven() {
        final StackFrame frame = mock(StackFrame.class);
        when(frame.isErroneous()).thenReturn(false);
        when(frame.isSuiteFileContext()).thenReturn(false);
        when(frame.isSuiteDirectoryContext()).thenReturn(false);

        final Image expected = ImagesManager.getImage(RedImages.getStackFrameImage());

        final RobotStackFrame stackFrame = new RobotStackFrame(mock(RobotThread.class), frame, null);
        assertThat(presentation.getImage(stackFrame)).isEqualTo(expected);
    }

    @Test
    public void errorMarkerDecoratedStackFrameImageIsReturned_whenNormalErroneousStackFrameIsGiven() {
        final StackFrame frame = mock(StackFrame.class);
        when(frame.isErroneous()).thenReturn(true);
        when(frame.isSuiteFileContext()).thenReturn(false);
        when(frame.isSuiteDirectoryContext()).thenReturn(false);

        final Image expected = ImagesManager
                .getImage(new DecorationOverlayIcon(ImagesManager.getImage(RedImages.getStackFrameImage()),
                        RedImages.getErrorImage(), IDecoration.TOP_LEFT));

        final RobotStackFrame stackFrame = new RobotStackFrame(mock(RobotThread.class), frame, null);
        assertThat(presentation.getImage(stackFrame)).isEqualTo(expected);
    }

    @Test
    public void suiteDirectoryMarkerDecoratedStackFrameImageIsReturned_whenStackFrameForDirectorySuiteIsGiven() {
        final StackFrame frame = mock(StackFrame.class);
        when(frame.isErroneous()).thenReturn(false);
        when(frame.isSuiteFileContext()).thenReturn(false);
        when(frame.isSuiteDirectoryContext()).thenReturn(true);

        final Image expected = ImagesManager
                .getImage(new DecorationOverlayIcon(ImagesManager.getImage(RedImages.getStackFrameImage()),
                        RedImages.Decorators.getFolderDecorator(), IDecoration.BOTTOM_RIGHT));

        final RobotStackFrame stackFrame = new RobotStackFrame(mock(RobotThread.class), frame, null);
        assertThat(presentation.getImage(stackFrame)).isEqualTo(expected);
    }

    @Test
    public void fileDirectoryMarkerDecoratedStackFrameImageIsReturned_whenStackFrameForFileSuiteIsGiven() {
        final StackFrame frame = mock(StackFrame.class);
        when(frame.isErroneous()).thenReturn(false);
        when(frame.isSuiteFileContext()).thenReturn(true);
        when(frame.isSuiteDirectoryContext()).thenReturn(false);

        final Image expected = ImagesManager
                .getImage(new DecorationOverlayIcon(ImagesManager.getImage(RedImages.getStackFrameImage()),
                        RedImages.Decorators.getRobotDecorator(), IDecoration.BOTTOM_RIGHT));

        final RobotStackFrame stackFrame = new RobotStackFrame(mock(RobotThread.class), frame, null);
        assertThat(presentation.getImage(stackFrame)).isEqualTo(expected);
    }

    @Test
    public void errorSuiteDirectoryMarkerDecoratedStackFrameImageIsReturned_whenErroneousStackFrameForDirectorySuiteIsGiven() {
        final StackFrame frame = mock(StackFrame.class);
        when(frame.isErroneous()).thenReturn(true);
        when(frame.isSuiteFileContext()).thenReturn(false);
        when(frame.isSuiteDirectoryContext()).thenReturn(true);

        final Image expected = ImagesManager
                .getImage(new DecorationOverlayIcon(ImagesManager.getImage(RedImages.getStackFrameImage()),
                        new ImageDescriptor[] { RedImages.getErrorImage(), null, null,
                                RedImages.Decorators.getFolderDecorator(), null }));

        final RobotStackFrame stackFrame = new RobotStackFrame(mock(RobotThread.class), frame, null);
        assertThat(presentation.getImage(stackFrame)).isEqualTo(expected);
    }

    @Test
    public void errorFileDirectoryMarkerDecoratedStackFrameImageIsReturned_whenErroneousStackFrameForFileSuiteIsGiven() {
        final StackFrame frame = mock(StackFrame.class);
        when(frame.isErroneous()).thenReturn(true);
        when(frame.isSuiteFileContext()).thenReturn(true);
        when(frame.isSuiteDirectoryContext()).thenReturn(false);

        final Image expected = ImagesManager
                .getImage(new DecorationOverlayIcon(ImagesManager.getImage(RedImages.getStackFrameImage()),
                        new ImageDescriptor[] { RedImages.getErrorImage(), null, null,
                                RedImages.Decorators.getRobotDecorator(), null }));

        final RobotStackFrame stackFrame = new RobotStackFrame(mock(RobotThread.class), frame, null);
        assertThat(presentation.getImage(stackFrame)).isEqualTo(expected);
    }

    @Test
    public void scalarWithGlobalScopeDecoratorImageIsReturned_whenScalarInGlobalScopeIsGiven() {
        final Image expected = ImagesManager.getImage(
                new DecorationOverlayIcon(ImagesManager.getImage(RedImages.VARIABLES.getDebugScalarVariableImage()),
                        RedImages.VARIABLES.getDebugGlobalScopeDecorator(), IDecoration.TOP_RIGHT));

        final StackFrameVariable stackVariable = new StackFrameVariable(VariableScope.GLOBAL, true, "var", "int", 42);
        final RobotDebugVariable variable = new RobotDebugVariable(mock(RobotStackFrame.class), stackVariable);
        assertThat(presentation.getImage(variable)).isEqualTo(expected);
    }

    @Test
    public void listWithGlobalScopeDecoratorImageIsReturned_whenListInGlobalScopeIsGiven() {
        final Image expected = ImagesManager.getImage(
                new DecorationOverlayIcon(ImagesManager.getImage(RedImages.VARIABLES.getDebugListVariableImage()),
                        RedImages.VARIABLES.getDebugGlobalScopeDecorator(), IDecoration.TOP_RIGHT));

        final StackFrameVariable stackVariable = new StackFrameVariable(VariableScope.GLOBAL, true, "var", "list",
                newArrayList(new VariableTypedValue("string", "abc")));
        final RobotDebugVariable variable = new RobotDebugVariable(mock(RobotStackFrame.class), stackVariable);
        assertThat(presentation.getImage(variable)).isEqualTo(expected);
    }

    @Test
    public void dictionaryWithGlobalScopeDecoratorImageIsReturned_whenDictionaryInGlobalScopeIsGiven() {
        final Image expected = ImagesManager.getImage(
                new DecorationOverlayIcon(ImagesManager.getImage(RedImages.VARIABLES.getDebugDictionaryVariableImage()),
                        RedImages.VARIABLES.getDebugGlobalScopeDecorator(), IDecoration.TOP_RIGHT));

        final StackFrameVariable stackVariable = new StackFrameVariable(VariableScope.GLOBAL, true, "var", "dict",
                ImmutableMap.of("a", new VariableTypedValue("int", 1)));
        final RobotDebugVariable variable = new RobotDebugVariable(mock(RobotStackFrame.class), stackVariable);
        assertThat(presentation.getImage(variable)).isEqualTo(expected);
    }

    @Test
    public void scalarWithSuiteScopeDecoratorImageIsReturned_whenScalarInSuiteScopeIsGiven() {
        final Image expected = ImagesManager.getImage(
                new DecorationOverlayIcon(ImagesManager.getImage(RedImages.VARIABLES.getDebugScalarVariableImage()),
                        RedImages.VARIABLES.getDebugSuiteScopeDecorator(), IDecoration.TOP_RIGHT));

        final StackFrameVariable stackVariable = new StackFrameVariable(VariableScope.TEST_SUITE, true, "var", "int",
                42);
        final RobotDebugVariable variable = new RobotDebugVariable(mock(RobotStackFrame.class), stackVariable);
        assertThat(presentation.getImage(variable)).isEqualTo(expected);
    }

    @Test
    public void listWithSuiteScopeDecoratorImageIsReturned_whenListInSuiteScopeIsGiven() {
        final Image expected = ImagesManager.getImage(
                new DecorationOverlayIcon(ImagesManager.getImage(RedImages.VARIABLES.getDebugListVariableImage()),
                        RedImages.VARIABLES.getDebugSuiteScopeDecorator(), IDecoration.TOP_RIGHT));

        final StackFrameVariable stackVariable = new StackFrameVariable(VariableScope.TEST_SUITE, true, "var", "list",
                newArrayList(new VariableTypedValue("string", "abc")));
        final RobotDebugVariable variable = new RobotDebugVariable(mock(RobotStackFrame.class), stackVariable);
        assertThat(presentation.getImage(variable)).isEqualTo(expected);
    }

    @Test
    public void dictionaryWithSuiteScopeDecoratorImageIsReturned_whenDictionaryInSuiteScopeIsGiven() {
        final Image expected = ImagesManager.getImage(
                new DecorationOverlayIcon(ImagesManager.getImage(RedImages.VARIABLES.getDebugDictionaryVariableImage()),
                        RedImages.VARIABLES.getDebugSuiteScopeDecorator(), IDecoration.TOP_RIGHT));

        final StackFrameVariable stackVariable = new StackFrameVariable(VariableScope.TEST_SUITE, true, "var", "dict",
                ImmutableMap.of("a", new VariableTypedValue("int", 1)));
        final RobotDebugVariable variable = new RobotDebugVariable(mock(RobotStackFrame.class), stackVariable);
        assertThat(presentation.getImage(variable)).isEqualTo(expected);
    }

    @Test
    public void scalarWithTestScopeDecoratorImageIsReturned_whenScalarInTestScopeIsGiven() {
        final Image expected = ImagesManager.getImage(
                new DecorationOverlayIcon(ImagesManager.getImage(RedImages.VARIABLES.getDebugScalarVariableImage()),
                        RedImages.VARIABLES.getDebugTestScopeDecorator(), IDecoration.TOP_RIGHT));

        final StackFrameVariable stackVariable = new StackFrameVariable(VariableScope.TEST_CASE, true, "var", "int",
                42);
        final RobotDebugVariable variable = new RobotDebugVariable(mock(RobotStackFrame.class), stackVariable);
        assertThat(presentation.getImage(variable)).isEqualTo(expected);
    }

    @Test
    public void listWithTestScopeDecoratorImageIsReturned_whenListInTestScopeIsGiven() {
        final Image expected = ImagesManager.getImage(
                new DecorationOverlayIcon(ImagesManager.getImage(RedImages.VARIABLES.getDebugListVariableImage()),
                        RedImages.VARIABLES.getDebugTestScopeDecorator(), IDecoration.TOP_RIGHT));

        final StackFrameVariable stackVariable = new StackFrameVariable(VariableScope.TEST_CASE, true, "var", "list",
                newArrayList(new VariableTypedValue("string", "abc")));
        final RobotDebugVariable variable = new RobotDebugVariable(mock(RobotStackFrame.class), stackVariable);
        assertThat(presentation.getImage(variable)).isEqualTo(expected);
    }

    @Test
    public void dictionaryWithTestScopeDecoratorImageIsReturned_whenDictionaryInTestScopeIsGiven() {
        final Image expected = ImagesManager.getImage(
                new DecorationOverlayIcon(ImagesManager.getImage(RedImages.VARIABLES.getDebugDictionaryVariableImage()),
                        RedImages.VARIABLES.getDebugTestScopeDecorator(), IDecoration.TOP_RIGHT));

        final StackFrameVariable stackVariable = new StackFrameVariable(VariableScope.TEST_CASE, true, "var", "dict",
                ImmutableMap.of("a", new VariableTypedValue("int", 1)));
        final RobotDebugVariable variable = new RobotDebugVariable(mock(RobotStackFrame.class), stackVariable);
        assertThat(presentation.getImage(variable)).isEqualTo(expected);
    }

    @Test
    public void scalarWithLocalScopeDecoratorImageIsReturned_whenScalarInLocalScopeIsGiven() {
        final Image expected = ImagesManager.getImage(
                new DecorationOverlayIcon(ImagesManager.getImage(RedImages.VARIABLES.getDebugScalarVariableImage()),
                        RedImages.VARIABLES.getDebugLocalScopeDecorator(), IDecoration.TOP_RIGHT));

        final StackFrameVariable stackVariable = new StackFrameVariable(VariableScope.LOCAL, true, "var", "int",
                42);
        final RobotDebugVariable variable = new RobotDebugVariable(mock(RobotStackFrame.class), stackVariable);
        assertThat(presentation.getImage(variable)).isEqualTo(expected);
    }

    @Test
    public void listWithLocalScopeDecoratorImageIsReturned_whenListInLocalScopeIsGiven() {
        final Image expected = ImagesManager.getImage(
                new DecorationOverlayIcon(ImagesManager.getImage(RedImages.VARIABLES.getDebugListVariableImage()),
                        RedImages.VARIABLES.getDebugLocalScopeDecorator(), IDecoration.TOP_RIGHT));

        final StackFrameVariable stackVariable = new StackFrameVariable(VariableScope.LOCAL, true, "var", "list",
                newArrayList(new VariableTypedValue("string", "abc")));
        final RobotDebugVariable variable = new RobotDebugVariable(mock(RobotStackFrame.class), stackVariable);
        assertThat(presentation.getImage(variable)).isEqualTo(expected);
    }

    @Test
    public void dictionaryWithLocalScopeDecoratorImageIsReturned_whenDictionaryInLocalScopeIsGiven() {
        final Image expected = ImagesManager.getImage(
                new DecorationOverlayIcon(ImagesManager.getImage(RedImages.VARIABLES.getDebugDictionaryVariableImage()),
                        RedImages.VARIABLES.getDebugLocalScopeDecorator(), IDecoration.TOP_RIGHT));

        final StackFrameVariable stackVariable = new StackFrameVariable(VariableScope.LOCAL, true, "var", "dict",
                ImmutableMap.of("a", new VariableTypedValue("int", 1)));
        final RobotDebugVariable variable = new RobotDebugVariable(mock(RobotStackFrame.class), stackVariable);
        assertThat(presentation.getImage(variable)).isEqualTo(expected);
    }

    @Test
    public void elementImageIsReturned_whenVariableIsAGroupingArtificialElement() {
        final Image expected = ImagesManager.getImage(RedImages.getElementImage());

        final RobotDebugVariable variable = RobotDebugVariable.createAutomatic(mock(RobotStackFrame.class),
                new ArrayList<>());
        assertThat(presentation.getImage(variable)).isEqualTo(expected);

    }

    @Test
    public void threadNameIsProvided_whenThreadIsGiven() throws CoreException {
        final IThread thread = mock(IThread.class);
        when(thread.getName()).thenReturn("robot thread");

        assertThat(presentation.getText(thread)).isEqualTo("robot thread");
    }

    @Test
    public void debugTargetNameIsProvided_whenDebugTargetIsGiven() throws CoreException {
        final IDebugTarget target = mock(IDebugTarget.class);
        when(target.getName()).thenReturn("target");

        assertThat(presentation.getText(target)).isEqualTo("target");
    }

    @Test
    public void stackframeNameIsProvided_whenStackframeIsGiven() throws CoreException {
        final RobotStackFrame frame = mock(RobotStackFrame.class);
        when(frame.getLabel()).thenReturn("frame");

        assertThat(presentation.getText(frame)).isEqualTo("frame");
    }

    @Test
    public void breakpointLabelIsProvided_whenRobotLineBreakpointIsGiven() throws CoreException {
        final RobotLineBreakpoint breakpoint = mock(RobotLineBreakpoint.class);
        when(breakpoint.getLabel()).thenReturn("breakpoint [line: 3]");

        assertThat(presentation.getText(breakpoint)).isEqualTo("breakpoint [line: 3]");
    }

    @Test
    public void redNameIsProvided_whenArbitraryObjectIsGiven() throws CoreException {
        assertThat(presentation.getText(new Object())).isEqualTo("RED");
    }

    @Test
    public void robotSuiteEditorIdIsProvided_whenFileIsGiven() {
        final IFile file = mock(IFile.class);
        final String id = presentation.getEditorId(mock(IEditorInput.class), file);

        assertThat(id).isEqualTo(RobotFormEditor.ID);
    }

    @Test
    public void fileEditorInputIsProvided_whenFileIsGiven() {
        final IFile file = mock(IFile.class);
        final IEditorInput input = presentation.getEditorInput(file);

        assertThat(input).isInstanceOf(IFileEditorInput.class);
        assertThat(((IFileEditorInput) input).getFile()).isSameAs(file);
    }

    @Test
    public void robotSuiteEditorIdIsProvided_whenLineBreakpointIsGiven() {
        final ILineBreakpoint breakpoint = mock(ILineBreakpoint.class);
        final String id = presentation.getEditorId(mock(IEditorInput.class), breakpoint);

        assertThat(id).isEqualTo(RobotFormEditor.ID);
    }

    @Test
    public void fileEditorInputIsProvided_whenLineBreakpointIsGiven() {
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
    public void debuggerAssistantEditorIdIsProvided_whenSourceOfStackFrameNotFoundElementIsGiven() {
        final SourceOfStackFrameNotFound element = new SourceOfStackFrameNotFound("frame", "text");
        final String id = presentation.getEditorId(mock(IEditorInput.class), element);

        assertThat(id).isEqualTo(RedDebuggerAssistantEditorWrapper.ID);
    }

    @Test
    public void sourceNotFoundEditorInputIsProvided_whenSourceOfStackFrameNotFoundElementIsGiven() {
        final SourceOfStackFrameNotFound element = new SourceOfStackFrameNotFound("frame", "text");

        final IEditorInput input = presentation.getEditorInput(element);

        assertThat(input).isInstanceOf(SourceNotFoundEditorInput.class);
        assertThat(((SourceNotFoundEditorInput) input).getElement()).isSameAs(element);
    }

    @Test
    public void debuggerAssistantEditorIdIsProvided_whenSourceOfStackFrameInLibraryElementIsGiven() {
        final SourceOfStackFrameInLibrary element = new SourceOfStackFrameInLibrary("frame",
                URI.create("file://suite.robot"));
        final String id = presentation.getEditorId(mock(IEditorInput.class), element);

        assertThat(id).isEqualTo(RedDebuggerAssistantEditorWrapper.ID);
    }

    @Test
    public void sourceInLibraryEditorInput_whenSourceOfStackFrameInLibraryElementIsGiven() {
        final SourceOfStackFrameInLibrary element = new SourceOfStackFrameInLibrary("frame",
                URI.create("file://suite.robot"));

        final IEditorInput input = presentation.getEditorInput(element);

        assertThat(input).isInstanceOf(SourceInLibraryEditorInput.class);
        assertThat(((SourceInLibraryEditorInput) input).getElement()).isSameAs(element);
    }

    @Test
    public void noEditorIdIsProvided_whenArbitraryObjectIsGiven() {
        final String id = presentation.getEditorId(mock(IEditorInput.class), new Object());
        assertThat(id).isNull();
    }

    @Test
    public void noEditorInputIsProvided_whenArbitraryObjectIsGiven() {
        final IEditorInput input = presentation.getEditorInput(new Object());
        assertThat(input).isNull();
    }

    @Test
    public void whenRobotDebugValueIsComputed_listenerIsNotifiedAboutIt() {
        final IValueDetailListener listener = mock(IValueDetailListener.class);

        final StackFrameVariable variable1 = new StackFrameVariable(VariableScope.LOCAL, false, "scalar", "str", "val");
        final StackFrameVariable variable2 = new StackFrameVariable(VariableScope.LOCAL, false, "list", "list",
                newArrayList(new VariableTypedValue("str", "x"), new VariableTypedValue("str", "y"),
                        new VariableTypedValue("str", "z")));
        final StackFrameVariable variable3 = new StackFrameVariable(VariableScope.LOCAL, false, "map", "dict",
                ImmutableMap.of("k1", new VariableTypedValue("str", "x"), "k2", new VariableTypedValue("str", "y"),
                        "k3", new VariableTypedValue("str", "z")));

        final IValue value1 = new RobotDebugVariable(mock(RobotStackFrame.class), variable1).getValue();
        final IValue value2 = new RobotDebugVariable(mock(RobotStackFrame.class), variable2).getValue();
        final IValue value3 = new RobotDebugVariable(mock(RobotStackFrame.class), variable3).getValue();

        presentation.computeDetail(value1, listener);
        presentation.computeDetail(value2, listener);
        presentation.computeDetail(value3, listener);

        verify(listener).detailComputed(value1, "val");
        verify(listener).detailComputed(value2, "[x, y, z]");
        verify(listener).detailComputed(value3, "{k1=x, k2=y, k3=z}");
    }

    @Test
    public void nothingHappens_whenDetailsIsComputedForOrdinaryValue() {
        final RobotModelPresentation presentation = spy(new RobotModelPresentation());
        final IValueDetailListener listener = mock(IValueDetailListener.class);
        final IValue value = mock(IValue.class);

        presentation.computeDetail(value, listener);
        
        verify(presentation).computeDetail(value, listener);
        verifyNoMoreInteractions(presentation);
        verifyZeroInteractions(listener, value);
    }

    @Test
    public void annotationTypeForPrimaryErroneousInstructionPointerIsProvided() {
        final RobotThread thread = mock(RobotThread.class);

        final StackFrame stackFrame = mock(StackFrame.class);
        when(stackFrame.isErroneous()).thenReturn(true);

        final RobotStackFrame frame = new RobotStackFrame(thread, stackFrame, null);
        when(thread.getTopStackFrame()).thenReturn(frame);

        final String typeId = presentation.getInstructionPointerAnnotationType(mock(IEditorPart.class), frame);
        assertThat(typeId).isEqualTo(RobotModelPresentation.RED_DEBUG_ERROR_CURRENT_IP);
    }

    @Test
    public void annotationTypeForSecondaryErroneousInstructionPointerIsProvided() {
        final RobotThread thread = mock(RobotThread.class);

        final StackFrame stackFrame = mock(StackFrame.class);
        when(stackFrame.isErroneous()).thenReturn(true);

        final RobotStackFrame frame = new RobotStackFrame(thread, stackFrame, null);
        when(thread.getTopStackFrame()).thenReturn(mock(RobotStackFrame.class));

        final String typeId = presentation.getInstructionPointerAnnotationType(mock(IEditorPart.class), frame);
        assertThat(typeId).isEqualTo(RobotModelPresentation.RED_DEBUG_ERROR_SECONDARY_IP);
    }

    @Test
    public void noAnnotationTypeIsProvidedForCorrectStackFrame() {
        final RobotThread thread = mock(RobotThread.class);

        final StackFrame stackFrame = mock(StackFrame.class);
        when(stackFrame.isErroneous()).thenReturn(false);

        final RobotStackFrame frame = new RobotStackFrame(thread, stackFrame, null);
        when(thread.getTopStackFrame()).thenReturn(frame);

        final String typeId = presentation.getInstructionPointerAnnotationType(mock(IEditorPart.class), frame);
        assertThat(typeId).isNull();
    }

    @Test
    public void noAnnotationTypeIsProvidedForOrdinaryStackFrame() {
        final String typeId = presentation.getInstructionPointerAnnotationType(mock(IEditorPart.class),
                mock(IStackFrame.class));
        assertThat(typeId).isNull();
    }

    @Test
    public void noAnnotationIsCreatedForOrdinaryStackFrame() {
        final Annotation annotation = presentation.getInstructionPointerAnnotation(mock(IEditorPart.class),
                mock(IStackFrame.class));
        assertThat(annotation).isNull();
    }

    @Test
    public void noAnnotationIsCreatedEvenForErroneousRobotStackFrame() {
        final RobotThread thread = mock(RobotThread.class);

        final StackFrame stackFrame = mock(StackFrame.class);
        when(stackFrame.isErroneous()).thenReturn(true);

        final RobotStackFrame frame = new RobotStackFrame(thread, stackFrame, null);
        when(thread.getTopStackFrame()).thenReturn(frame);

        final Annotation annotation = presentation.getInstructionPointerAnnotation(mock(IEditorPart.class), frame);
        assertThat(annotation).isNull();
    }

    @Test
    public void noImageIsProvidedForOrdinaryStackFrameAnnotation() {
        final Image image = presentation.getInstructionPointerImage(mock(IEditorPart.class),
                mock(IStackFrame.class));
        assertThat(image).isNull();
    }

    @Test
    public void noImageIsProvidedForOrdinaryStackFrameAnnotationEvenForErroneousRobotStackFrame() {
        final RobotThread thread = mock(RobotThread.class);

        final StackFrame stackFrame = mock(StackFrame.class);
        when(stackFrame.isErroneous()).thenReturn(true);

        final RobotStackFrame frame = new RobotStackFrame(thread, stackFrame, null);
        when(thread.getTopStackFrame()).thenReturn(frame);

        final Image image = presentation.getInstructionPointerImage(mock(IEditorPart.class), frame);
        assertThat(image).isNull();
    }

    @Test
    public void noInstructionPointerTextIsProvidedForOrdinaryFrame() {
        final String text = presentation.getInstructionPointerText(mock(IEditorPart.class), mock(IStackFrame.class));
        assertThat(text).isNull();
    }

    @Test
    public void emptyInstructionPointerTextIsProvidedForStackFrameWithEmptyText() {
        final StackFrame stackFrame = mock(StackFrame.class);
        when(stackFrame.isErroneous()).thenReturn(true);

        final RobotStackFrame frame = spy(new RobotStackFrame(mock(RobotThread.class), stackFrame, null));
        when(frame.getInstructionPointerText()).thenReturn("");

        final String text = presentation.getInstructionPointerText(mock(IEditorPart.class), frame);
        assertThat(text).isEmpty();
    }

    @Test
    public void nonEmptyPrefixedInstructionPointerTextIsProvidedForStackFrameWithNonEmptyText() {
        final StackFrame stackFrame = mock(StackFrame.class);
        when(stackFrame.isErroneous()).thenReturn(true);

        final RobotStackFrame frame = spy(new RobotStackFrame(mock(RobotThread.class), stackFrame, null));
        when(frame.getInstructionPointerText()).thenReturn("message");

        final String text = presentation.getInstructionPointerText(mock(IEditorPart.class), frame);
        assertThat(text).isEqualTo("RED debugger: message");
    }

    @Test
    public void suiteEditorHasSourcePageActivatedPriorToAddingAnnotations() {
        final RobotFormEditor editor = mock(RobotFormEditor.class);

        final boolean result = presentation.addAnnotations(editor, mock(IStackFrame.class));
        assertThat(result).isFalse();
        verify(editor).activateSourcePage();
        verifyNoMoreInteractions(editor);
    }

    @Test
    public void nothingHappensWhenAnnotationIsRemoved() {
        final IEditorPart editor = mock(IEditorPart.class);
        final IThread thread = mock(IThread.class);

        final RobotModelPresentation presentation = spy(new RobotModelPresentation());

        presentation.removeAnnotations(editor, thread);
        verifyZeroInteractions(editor, thread);
        verify(presentation).removeAnnotations(editor, thread);
        verifyNoMoreInteractions(presentation);
    }
}
