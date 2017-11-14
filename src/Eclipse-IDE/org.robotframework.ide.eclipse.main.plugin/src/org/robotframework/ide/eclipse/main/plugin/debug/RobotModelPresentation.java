/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.debug;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.ILineBreakpoint;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.ui.IDebugEditorPresentation;
import org.eclipse.debug.ui.IDebugModelPresentation;
import org.eclipse.debug.ui.IInstructionPointerPresentation;
import org.eclipse.debug.ui.IValueDetailListener;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.viewers.DecorationOverlayIcon;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.part.FileEditorInput;
import org.rf.ide.core.testdata.model.table.variables.AVariable.VariableScope;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.debug.SourceInLibraryEditorInput.SourceOfStackFrameInLibrary;
import org.robotframework.ide.eclipse.main.plugin.debug.SourceNotFoundEditorInput.SourceOfStackFrameNotFound;
import org.robotframework.ide.eclipse.main.plugin.debug.model.RobotDebugValue;
import org.robotframework.ide.eclipse.main.plugin.debug.model.RobotDebugVariable;
import org.robotframework.ide.eclipse.main.plugin.debug.model.RobotLineBreakpoint;
import org.robotframework.ide.eclipse.main.plugin.debug.model.RobotStackFrame;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotFormEditor;
import org.robotframework.red.graphics.ImagesManager;

/**
 * @author mmarzec
 *
 */
public class RobotModelPresentation extends LabelProvider
        implements IDebugModelPresentation, IDebugEditorPresentation, IInstructionPointerPresentation {

    public static final String RED_DEBUG_ERROR_SECONDARY_IP = "org.robotframework.red.debug.secondaryErrorIP";

    public static final String RED_DEBUG_ERROR_CURRENT_IP = "org.robotframework.red.debug.currentErrorIP";

    @Override
    public void setAttribute(final String attribute, final Object value) {
    }

    @Override
    public Image getImage(final Object element) {
        if (element instanceof RobotStackFrame) {
            final RobotStackFrame frame = (RobotStackFrame) element;
            final Image frameImage = ImagesManager.getImage(RedImages.getStackFrameImage());

            final ImageDescriptor[] overlays = new ImageDescriptor[5];
            fillTopLeftDecoration(overlays, frame);
            fillBottomRightDecoration(overlays, frame);
            return Stream.of(overlays).anyMatch(Objects::nonNull)
                    ? ImagesManager.getImage(new DecorationOverlayIcon(frameImage, overlays))
                    : frameImage;

        } else if (element instanceof RobotDebugVariable) {
            final RobotDebugVariable variable = (RobotDebugVariable) element;
            final Image varImage = ImagesManager.getImage(variable.getImage());

            ImageDescriptor decorator = null;
            final Optional<VariableScope> scope = variable.getScope();
            if (scope.isPresent()) {
                switch (scope.get()) {
                    case GLOBAL:
                        decorator = RedImages.VARIABLES.getDebugGlobalScopeDecorator();
                        break;
                    case TEST_SUITE:
                        decorator = RedImages.VARIABLES.getDebugSuiteScopeDecorator();
                        break;
                    case TEST_CASE:
                        decorator = RedImages.VARIABLES.getDebugTestScopeDecorator();
                        break;
                    case LOCAL:
                        decorator = RedImages.VARIABLES.getDebugLocalScopeDecorator();
                        break;
                    default:
                        throw new IllegalStateException();
                }
            }
            return decorator == null ? varImage
                    : ImagesManager.getImage(new DecorationOverlayIcon(varImage, decorator, IDecoration.TOP_RIGHT));
        }

        return null;
    }

    private void fillTopLeftDecoration(final ImageDescriptor[] overlays, final RobotStackFrame frame) {
        if (frame.isErroneous()) {
            overlays[IDecoration.TOP_LEFT] = RedImages.getErrorImage();
        }
    }

    private void fillBottomRightDecoration(final ImageDescriptor[] overlays, final RobotStackFrame frame) {
        if (frame.isSuiteDirectoryContext()) {
            overlays[IDecoration.BOTTOM_RIGHT] = RedImages.Decorators.getFolderDecorator();
        } else if (frame.isSuiteFileContext()) {
            overlays[IDecoration.BOTTOM_RIGHT] = RedImages.Decorators.getRobotDecorator();
        }
    }

    @Override
    public String getText(final Object element) {
        try {
            if (element instanceof IThread) {
                return ((IThread) element).getName();
            } else if (element instanceof IDebugTarget) {
                return ((IDebugTarget) element).getName();
            } else if (element instanceof RobotStackFrame) {
                return ((RobotStackFrame) element).getLabel();
            } else if (element instanceof RobotLineBreakpoint) {
                return ((RobotLineBreakpoint) element).getLabel();
            }
        } catch (final CoreException e) {
            e.printStackTrace();
        }
        return "RED";
    }

    @Override
    public void computeDetail(final IValue value, final IValueDetailListener listener) {
        if (value instanceof RobotDebugValue) {
            listener.detailComputed(value, ((RobotDebugValue) value).getDetailedValue());
        }
    }

    @Override
    public IEditorInput getEditorInput(final Object element) {
        if (element instanceof IFile) {
            return new FileEditorInput((IFile) element);
        } else if (element instanceof ILineBreakpoint) {
            return new FileEditorInput((IFile) ((ILineBreakpoint) element).getMarker().getResource());
        } else if (element instanceof SourceOfStackFrameNotFound) {
            return new SourceNotFoundEditorInput((SourceOfStackFrameNotFound) element);
        } else if (element instanceof SourceOfStackFrameInLibrary) {
            return new SourceInLibraryEditorInput((SourceOfStackFrameInLibrary) element);
        } else {
            return null;
        }
    }

    @Override
    public String getEditorId(final IEditorInput input, final Object element) {
        if (element instanceof IFile || element instanceof ILineBreakpoint) {
            return RobotFormEditor.ID;
        } else if (element instanceof SourceOfStackFrameNotFound || element instanceof SourceOfStackFrameInLibrary) {
            return RedDebuggerAssistantEditorWrapper.ID;
        } else {
            return null;
        }
    }

    @Override
    public String getInstructionPointerAnnotationType(final IEditorPart editorPart, final IStackFrame frame) {
        if (frame instanceof RobotStackFrame) {
            final RobotStackFrame robotStackFrame = (RobotStackFrame) frame;
            if (robotStackFrame.isErroneous()) {
                return robotStackFrame.isTopFrame() ? RED_DEBUG_ERROR_CURRENT_IP : RED_DEBUG_ERROR_SECONDARY_IP;
            }
        }
        return null;
    }

    @Override
    public Annotation getInstructionPointerAnnotation(final IEditorPart editorPart, final IStackFrame frame) {
        // let eclipse create proper annotation
        return null;
    }

    @Override
    public Image getInstructionPointerImage(final IEditorPart editorPart, final IStackFrame frame) {
        // let eclipse take the one defined in extension point
        return null;
    }

    @Override
    public String getInstructionPointerText(final IEditorPart editorPart, final IStackFrame frame) {
        if (frame instanceof RobotStackFrame) {
            final RobotStackFrame robotStackFrame = (RobotStackFrame) frame;
            final String message = robotStackFrame.getInstructionPointerText();
            return message.isEmpty() ? message : "RED debugger: " + message;
        }
        return null;
    }

    @Override
    public boolean addAnnotations(final IEditorPart editorPart, final IStackFrame frame) {
        // before placing instruction pointer annotations etc for given frame we have to activate
        // source page
        if (editorPart instanceof RobotFormEditor) {
            final RobotFormEditor editor = (RobotFormEditor) editorPart;
            editor.activateSourcePage();
        }
        return false; // no annotation added; eclipse will add standard
    }

    @Override
    public void removeAnnotations(final IEditorPart editorPart, final IThread thread) {
        // nothing to do
    }
}
