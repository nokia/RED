/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.source;

import static com.google.common.collect.Lists.newArrayList;

import java.util.Iterator;
import java.util.List;
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
import org.rf.ide.core.testdata.model.table.keywords.names.GherkinStyleSupport;
import org.rf.ide.core.testdata.model.table.keywords.names.GherkinStyleSupport.NameTransformation;
import org.rf.ide.core.testdata.model.table.variables.names.VariableNamesSupport;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.assist.RedKeywordProposal;
import org.robotframework.ide.eclipse.main.plugin.assist.RedKeywordProposals;
import org.robotframework.ide.eclipse.main.plugin.debug.model.RobotDebugTarget;
import org.robotframework.ide.eclipse.main.plugin.debug.model.RobotStackFrame;
import org.robotframework.ide.eclipse.main.plugin.debug.utils.RobotDebugValueManager;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfig.ReferencedLibrary;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotProblem;
import org.robotframework.ide.eclipse.main.plugin.project.library.LibrarySpecification;

import com.google.common.base.Joiner;
import com.google.common.base.Optional;

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
            return DocumentUtilities.findVariable(document, isTsv, offset)
                    .or(DocumentUtilities.findCellRegion(document, isTsv, offset))
                    .or(Optional.of(new Region(0, 0)))
                    .get();
        } catch (final BadLocationException e) {
            RedPlugin.logError(e.getMessage(), e);
            return new Region(0, 0);
        }
    }

    @Override
    public Object getHoverInfo2(final ITextViewer textViewer, final IRegion hoverRegion) {
        try {
            final String problem = getErrorMsgs(textViewer, hoverRegion);
            if (problem != null) {
                return problem;
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

                final Optional<String> info = GherkinStyleSupport.firstNameTransformationResult(hoveredText,
                        new NameTransformation<String>() {
                            @Override
                            public Optional<String> transform(final String gherkinNameVariant) {
                                return Optional.fromNullable(getKeywordHoverInfo(gherkinNameVariant));
                            }
                        });
                return info.orNull();
            }
        } catch (final BadLocationException | CoreException e) {
        }
        return null;
    }

    private String getErrorMsgs(final ITextViewer textViewer, final IRegion hoverRegion) throws CoreException {
        final IAnnotationModel model = getAnnotationModel((ISourceViewer) textViewer);
        if (model == null) {
            return null;
        }

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
        } else {
            final String prefix = msgs.size() == 1 ? "" : "Multiple markers at this line:\n- ";
            return prefix + Joiner.on("\n- ").join(msgs);
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
                for (final IStackFrame stackFrame : robotTarget.getRobotThread().getStackFrames()) {
                    final RobotStackFrame robotStackFrame = (RobotStackFrame) stackFrame;
                    if (robotStackFrame.getFileName().equals(suiteFile.getFile().getName())) {
                        for (final IVariable variable : robotStackFrame.getAllVariables()) {
                            if (VariableNamesSupport.hasEqualNames(variable.getName(), variableName)) {
                                return "Current value:\n" + RobotDebugValueManager.extractValueDetail(variable.getValue());
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
        LibrarySpecification spec = suiteFile.getProject().getStandardLibraries().get(hoveredText);
        if (spec == null) {
            for (final ReferencedLibrary referencedLibrary : suiteFile.getProject().getReferencedLibraries().keySet()) {
                if (referencedLibrary.getName().equals(hoveredText)) {
                    spec = suiteFile.getProject().getReferencedLibraries().get(referencedLibrary);
                    break;
                }
            }
        }
        return spec == null ? null : spec.getDocumentation();
    }

    private String getKeywordHoverInfo(final String keywordName) {
        final RedKeywordProposals proposals = new RedKeywordProposals(suiteFile);

        final RedKeywordProposal best = proposals.getBestMatchingKeywordProposal(keywordName);
        if (best != null) {
            return best.getDocumentation();
        }
        return null;
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
