/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.source;

import static com.google.common.collect.Lists.newArrayList;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DefaultInformationControl;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IInformationControl;
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
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.texteditor.MarkerAnnotation;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.table.keywords.names.GherkinStyleSupport;
import org.rf.ide.core.testdata.model.table.variables.names.VariableNamesSupport;
import org.rf.ide.core.testdata.text.read.IRobotLineElement;
import org.rf.ide.core.testdata.text.read.RobotLine;
import org.rf.ide.core.testdata.text.read.RobotLine.PositionCheck;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.assist.RedKeywordProposal;
import org.robotframework.ide.eclipse.main.plugin.assist.RedKeywordProposals;
import org.robotframework.ide.eclipse.main.plugin.debug.RobotModelPresentation;
import org.robotframework.ide.eclipse.main.plugin.debug.model.RobotDebugTarget;
import org.robotframework.ide.eclipse.main.plugin.debug.model.RobotDebugVariable;
import org.robotframework.ide.eclipse.main.plugin.debug.model.RobotStackFrame;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotProblem;
import org.robotframework.ide.eclipse.main.plugin.project.library.LibrarySpecification;

import com.google.common.base.Joiner;
import com.google.common.base.Objects;

public class SuiteSourceHoverSupport implements ITextHover, ITextHoverExtension, ITextHoverExtension2 {

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

            Optional<IRegion> region = DocumentUtilities.findVariable(document, isTsv, offset);
            if (!region.isPresent()) {
                region = DocumentUtilities.findCellRegion(document, isTsv, offset);
                if (!region.isPresent()) {
                    region = Optional.of(new Region(offset, 0));
                }
            }
            return region.get();
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
                if (isLibrary(document, hoverRegion.getOffset())) {
                    final String libInfo = getLibraryHoverInfo(hoveredText);
                    if (libInfo != null) {
                        return libInfo;
                    }
                }

                if (isKeyword(suiteFile, hoverRegion)) {
                    final Optional<String> info = GherkinStyleSupport.firstNameTransformationResult(hoveredText,
                            this::getKeywordHoverInfo);
                    return info.orElse(null);
                }
            }
        } catch (final BadLocationException | CoreException e) {
        }
        return null;
    }

    private boolean isKeyword(final RobotSuiteFile suiteFile, final IRegion hoverRegion) {
        final RobotFile model = suiteFile.getLinkedElement();
        final Optional<Integer> lineIndex = model.getRobotLineIndexBy(hoverRegion.getOffset());
        if (lineIndex.isPresent()) {
            final RobotLine robotLine = model.getFileContent().get(lineIndex.get());
            final Optional<Integer> elementPositionInLine = robotLine.getElementPositionInLine(hoverRegion.getOffset(),
                    PositionCheck.INSIDE);
            if (elementPositionInLine.isPresent()) {
                final IRobotLineElement hoveredElement = robotLine.getLineElements().get(elementPositionInLine.get());
                return (!hoveredElement.getTypes().contains(RobotTokenType.TEST_CASE_NAME));
            }
        }

        return false;
    }

    private String getAnnotationInfo(final ITextViewer textViewer, final IRegion hoverRegion) throws CoreException {
        final IAnnotationModel model = getAnnotationModel((ISourceViewer) textViewer);
        if (model == null) {
            return null;
        }
        final String debuggerError = getInstructionPointerErrorMsgs(model, hoverRegion);
        if (debuggerError != null) {
            return debuggerError;
        }
        return getErrorMsgs(model, hoverRegion);

    }

    private String getInstructionPointerErrorMsgs(final IAnnotationModel model, final IRegion hoverRegion)
            throws CoreException {
        final Iterator<?> iter = model.getAnnotationIterator();
        while (iter.hasNext()) {
            final Annotation annotation = (Annotation) iter.next();
            if (RobotModelPresentation.RED_DEBUG_ERROR_CURRENT_IP.equals(annotation.getType())
                    || RobotModelPresentation.RED_DEBUG_ERROR_SECONDARY_IP.equals(annotation.getType())) {
                final Position position = model.getPosition(annotation);
                if (position != null && position.overlapsWith(hoverRegion.getOffset(), hoverRegion.getLength())) {
                    final String msg = annotation.getText();
                    if (msg != null && msg.trim().length() > 0) {
                        return msg.trim();
                    }
                }
            }
        }
        return null;
    }

    private String getErrorMsgs(final IAnnotationModel model, final IRegion hoverRegion)
            throws CoreException {
        final List<String> msgs = newArrayList();
        final Iterator<?> iter = model.getAnnotationIterator();
        while (iter.hasNext()) {
            final Annotation annotation = (Annotation) iter.next();
            if (isAnnotationSupported(annotation)) {
                final Position position = model.getPosition(annotation);
                if (position != null && position.overlapsWith(hoverRegion.getOffset(), hoverRegion.getLength())) {
                    final String msg = annotation.getText();
                    if (msg != null && msg.trim().length() > 0) {
                        msgs.add(msg);
                    }
                }
            }
        }
        if (msgs.isEmpty()) {
            return null;
        } else if (msgs.size() == 1) {
            return msgs.get(0);
        } else {
            return "Multiple markers at this line:\n- " + Joiner.on("\n- ").join(msgs);
        }
    }

    private boolean isAnnotationSupported(final Annotation annotation) throws CoreException {
        return annotation instanceof MarkerAnnotation
                && RobotProblem.TYPE_ID.equals(((MarkerAnnotation) annotation).getMarker().getType());
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
                                return "Current value:\n" + value;
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    private boolean isLibrary(final IDocument document, final int offset) {
        final String lineContent = DocumentUtilities.lineContentBeforeCurrentPosition(document, offset);
        return lineContent.trim().startsWith("Library");
    }

    private String getLibraryHoverInfo(final String hoveredText) {
        return suiteFile.getProject()
                .getLibrarySpecificationsStream()
                .filter(spec -> spec.getName().equalsIgnoreCase(hoveredText))
                .map(LibrarySpecification::getName)
                .findFirst()
                .orElse(null);
    }

    private Optional<String> getKeywordHoverInfo(final String keywordName) {
        final Optional<RedKeywordProposal> bestMatch = new RedKeywordProposals(suiteFile)
                .getBestMatchingKeywordProposal(keywordName);
        return bestMatch.map(RedKeywordProposal::getDescription);
    }

    private IAnnotationModel getAnnotationModel(final ISourceViewer viewer) {
        if (viewer instanceof ISourceViewerExtension2) {
            final ISourceViewerExtension2 extension = (ISourceViewerExtension2) viewer;
            return extension.getVisualAnnotationModel();
        }
        return viewer.getAnnotationModel();
    }

    @Override
    public IInformationControlCreator getHoverControlCreator() {
        return new IInformationControlCreator() {

            @Override
            public IInformationControl createInformationControl(final Shell parent) {
                return new DefaultInformationControl(parent);
            }
        };
    }
}
