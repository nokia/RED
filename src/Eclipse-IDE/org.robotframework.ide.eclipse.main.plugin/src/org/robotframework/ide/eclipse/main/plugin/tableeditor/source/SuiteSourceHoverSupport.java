/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.source;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.ITextHoverExtension;
import org.eclipse.jface.text.ITextHoverExtension2;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.ISourceViewerExtension2;
import org.eclipse.ui.texteditor.MarkerAnnotation;
import org.rf.ide.core.testdata.model.table.variables.names.VariableNamesSupport;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.debug.RobotModelPresentation;
import org.robotframework.ide.eclipse.main.plugin.debug.model.RobotDebugTarget;
import org.robotframework.ide.eclipse.main.plugin.debug.model.RobotDebugVariable;
import org.robotframework.ide.eclipse.main.plugin.debug.model.RobotStackFrame;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotProblem;
import org.robotframework.ide.eclipse.main.plugin.views.documentation.Documentations;
import org.robotframework.ide.eclipse.main.plugin.views.documentation.DocumentationsFormatter;
import org.robotframework.ide.eclipse.main.plugin.views.documentation.inputs.DocumentationViewInput;

import com.google.common.base.Objects;
import com.google.common.collect.Streams;

public class SuiteSourceHoverSupport implements ITextHover, ITextHoverExtension, ITextHoverExtension2 {

    private final InformationControlSupport infoSupport = new InformationControlSupport("Press 'F2' for focus");

    private final RobotSuiteFile suiteFile;

    public SuiteSourceHoverSupport(final RobotSuiteFile file) {
        this.suiteFile = file;
    }

    @Deprecated
    @Override
    public String getHoverInfo(final ITextViewer textViewer, final IRegion hoverRegion) {
        return null;
    }

    @Override
    public IRegion getHoverRegion(final ITextViewer textViewer, final int offset) {
        try {
            final IDocument document = textViewer.getDocument();
            final boolean isTsv = suiteFile.isTsvFile();

            final Optional<IRegion> region = DocumentUtilities.findVariable(document, isTsv, offset);
            if (region.isPresent()) {
                return region.get();
            }
            return DocumentUtilities.findCellRegion(document, isTsv, offset).orElseGet(() -> new Region(offset, 0));

        } catch (final BadLocationException e) {
            RedPlugin.logError(e.getMessage(), e);
            return new Region(0, 0);
        }
    }

    @Override
    public Object getHoverInfo2(final ITextViewer textViewer, final IRegion hoverRegion) {
        try {
            final String problem = getAnnotationInfo(textViewer, hoverRegion);
            if (problem != null) {
                return problem;
            }
            if (hoverRegion.getLength() == 0) {
                return null;
            }

            final IDocument document = textViewer.getDocument();
            final String hoveredText = document.get(hoverRegion.getOffset(), hoverRegion.getLength());
            if (isVariable(hoveredText)) {
                return getVariableHoverInfo(hoveredText);
            } else {
                return getElementDocumentation(hoverRegion.getOffset(), hoveredText);
            }
        } catch (final BadLocationException | CoreException e) {
        }
        return null;
    }

    private String getAnnotationInfo(final ITextViewer textViewer, final IRegion hoverRegion) throws CoreException {
        final IAnnotationModel model = getAnnotationModel((ISourceViewer) textViewer);
        if (model == null) {
            return null;
        }
        return getInstructionPointerErrorMsgs(model, hoverRegion).map(this::formatMessage)
                .orElseGet(() -> getErrorMsgs(model, hoverRegion).map(this::formatMessage).orElse(null));
    }

    private String formatMessage(final String msg) {
        return infoSupport.isBrowserBased() ? DocumentationsFormatter.create(() -> msg) : msg;
    }

    private IAnnotationModel getAnnotationModel(final ISourceViewer viewer) {
        if (viewer instanceof ISourceViewerExtension2) {
            final ISourceViewerExtension2 extension = (ISourceViewerExtension2) viewer;
            return extension.getVisualAnnotationModel();
        }
        return viewer.getAnnotationModel();
    }

    private Optional<String> getInstructionPointerErrorMsgs(final IAnnotationModel model, final IRegion hoverRegion) {
        return annotationsWithMsgs(model, hoverRegion).filter(this::isRobotDebuggerAnnotation)
                .map(Annotation::getText)
                .findFirst();
    }

    private Optional<String> getErrorMsgs(final IAnnotationModel model, final IRegion hoverRegion) {
        final List<String> msgs = annotationsWithMsgs(model, hoverRegion).filter(this::isRobotErrorAnnotation)
                .map(Annotation::getText)
                .collect(toList());

        if (msgs.isEmpty()) {
            return Optional.empty();
        } else if (msgs.size() == 1) {
            return Optional.of(msgs.get(0));
        } else {
            if (infoSupport.isBrowserBased()) {
                return Optional
                        .of("<p style=\"margin:0;\"><b>Multiple markers at this line:</b></p>"
                            + "<ul style=\"margin-top:0;\">"
                            + msgs.stream().map(msg -> "<li>" + msg + "</li>").collect(joining())
                            + "</ul>");
            } else {
                return Optional.of("Multiple markers at this line:" + "\n- " + String.join("\n- ", msgs));
            }
        }
    }
    private boolean isRobotDebuggerAnnotation(final Annotation annotation) {
        return RobotModelPresentation.RED_DEBUG_ERROR_CURRENT_IP.equals(annotation.getType())
                || RobotModelPresentation.RED_DEBUG_ERROR_SECONDARY_IP.equals(annotation.getType());
    }

    private boolean isRobotErrorAnnotation(final Annotation annotation) {
        if (annotation instanceof MarkerAnnotation) {
            final MarkerAnnotation markerAnnotation = (MarkerAnnotation) annotation;
            try {
                return RobotProblem.TYPE_ID.equals(markerAnnotation.getMarker().getType());
            } catch (final CoreException e) {
                return false;
            }
        }
        return false;
    }

    private static Stream<Annotation> annotationsWithMsgs(final IAnnotationModel model, final IRegion hoverRegion) {
        return Streams.stream(model.getAnnotationIterator()).filter(annotation -> {
            final Position position = model.getPosition(annotation);
            return position != null && position.overlapsWith(hoverRegion.getOffset(), hoverRegion.getLength());
        }).filter(annotation -> annotation.getText() != null && annotation.getText().trim().length() > 0);
    }

    private static boolean isVariable(final String text) {
        return Pattern.matches("[@$&%]\\{.+\\}", text);
    }

    private String getVariableHoverInfo(final String variableName) throws DebugException {
        final ILaunchManager launchManager = DebugPlugin.getDefault().getLaunchManager();
        for (final IDebugTarget target : launchManager.getDebugTargets()) {
            if (target instanceof RobotDebugTarget) {
                final RobotDebugTarget robotTarget = (RobotDebugTarget) target;
                if (!robotTarget.isSuspended()) {
                    continue;
                }
                for (final IStackFrame stackFrame : robotTarget.getThread().getStackFrames()) {
                    final RobotStackFrame robotStackFrame = (RobotStackFrame) stackFrame;
                    if (Objects.equal(robotStackFrame.getPath(), Optional.of(suiteFile.getFile().getLocationURI()))) {
                        for (final IVariable variable : robotStackFrame.getAllVariables()) {
                            if (VariableNamesSupport.hasEqualNames(variable.getName(), variableName)) {
                                final String value = ((RobotDebugVariable) variable).getValue().getDetailedValue();
                                return formatVariableValue(value);
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    private String formatVariableValue(final String value) {
        if (infoSupport.isBrowserBased()) {
            return DocumentationsFormatter.create(() -> "<p style=\"margin:0;\"><b>Current value:</b></p>"
                    + "<pre style=\"font-family: monospace; font-size: small; background-color: inherit; margin-top:0;\">"
                    + value
                    + "</pre>");
        } else {
            return "Current value:\n" + value;
        }
    }

    private String getElementDocumentation(final int offset, final String hoveredText) {
        final Optional<DocumentationViewInput> docInput = Documentations
                .findDocumentationForEditorSourceSelection(suiteFile, offset, hoveredText);

        try {
            if (infoSupport.isBrowserBased()) {
                return docInput.map(DocumentationViewInput::provideHtml).orElse(null);
            } else {
                return docInput.map(DocumentationViewInput::provideRawText).orElse(null);
            }
        } catch (final RuntimeException e) {
            RedPlugin.logError("Unable to generate documentation", e);
            return null;
        }
    }

    @Override
    public IInformationControlCreator getHoverControlCreator() {
        return infoSupport.getHoverControlCreator();
    }
}
