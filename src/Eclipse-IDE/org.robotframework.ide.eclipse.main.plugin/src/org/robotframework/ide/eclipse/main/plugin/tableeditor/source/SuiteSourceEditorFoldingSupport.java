/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.source;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.projection.ProjectionAnnotation;
import org.eclipse.jface.text.source.projection.ProjectionAnnotationModel;
import org.rf.ide.core.testdata.model.ModelType;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.RedPreferences;
import org.robotframework.ide.eclipse.main.plugin.RedPreferences.FoldableElements;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCase;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCasesSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotDefinitionSetting;
import org.robotframework.ide.eclipse.main.plugin.model.RobotFileInternalElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordDefinition;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordsSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSettingsSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFileSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotTask;
import org.robotframework.ide.eclipse.main.plugin.model.RobotTasksSection;
import org.robotframework.red.swt.StyledTextWrapper;
import org.robotframework.red.swt.SwtThread;

import com.google.common.annotations.VisibleForTesting;

class SuiteSourceEditorFoldingSupport {

    private final RedPreferences preferences;

    private final StyledTextWrapper textControl;

    private final ProjectionAnnotationModel annotationsModel;

    private Map<Position, Annotation> oldFoldingAnnotations;

    SuiteSourceEditorFoldingSupport(final StyledTextWrapper textControl,
            final ProjectionAnnotationModel annotationsModel) {
        this(RedPlugin.getDefault().getPreferences(), textControl, annotationsModel);
    }

    @VisibleForTesting
    SuiteSourceEditorFoldingSupport(final RedPreferences preferences, final StyledTextWrapper textControl,
            final ProjectionAnnotationModel annotationsModel) {
        this.preferences = preferences;
        this.textControl = textControl;
        this.annotationsModel = annotationsModel;
        this.oldFoldingAnnotations = new HashMap<>();
    }

    void reset() {
        if (annotationsModel != null) {
            synchronized (annotationsModel) {
                resetFoldingStructure();
            }
            oldFoldingAnnotations.clear();
        }
    }

    void updateFoldingStructure(final RobotSuiteFile model, final IDocument document) {
        if (annotationsModel != null) {
            final Collection<Position> positions = calculateFoldingPositions(model, document);
            SwtThread.asyncExec(() -> {
                synchronized (annotationsModel) {
                    updateFoldingStructure(positions);
                }
            });
        }
    }

    @VisibleForTesting
    Collection<Position> calculateFoldingPositions(final RobotSuiteFile model, final IDocument document) {
        final Collection<Position> positions = new HashSet<>();

        final EnumSet<FoldableElements> foldableElements = preferences.getFoldableElements();
        if (foldableElements.contains(FoldableElements.SECTIONS)) {
            positions.addAll(calculateSectionsFoldingPositions(model));
        }
        if (foldableElements.contains(FoldableElements.CASES)) {
            positions.addAll(calculateSectionChildrenPositions(model, RobotCasesSection.class));
        }
        if (foldableElements.contains(FoldableElements.TASKS)) {
            positions.addAll(calculateSectionChildrenPositions(model, RobotTasksSection.class));
        }
        if (foldableElements.contains(FoldableElements.KEYWORDS)) {
            positions.addAll(calculateSectionChildrenPositions(model, RobotKeywordsSection.class));
        }
        if (foldableElements.contains(FoldableElements.DOCUMENTATION)) {
            positions.addAll(calculateDocumentationFoldingPositions(model));
        }

        return positions.stream()
                .filter(onlyPositionsSpanning(document, preferences.getFoldingLineLimit()))
                .map(nextLineShiftedPosition(document))
                .collect(toSet());
    }

    private Collection<Position> calculateSectionsFoldingPositions(final RobotSuiteFile model) {
        final Collection<Position> positions = new HashSet<>();

        for (final RobotSuiteFileSection section : model.getChildren()) {

            final Map<Position, Set<Position>> positionsGroupedByHeaders = new HashMap<>();
            final List<Position> headerOffsets = new ArrayList<>();
            for (final Position headerPosition : section.getPositions()) {
                headerOffsets.add(headerPosition);
                positionsGroupedByHeaders.put(headerPosition, new HashSet<Position>());
            }
            Collections.sort(headerOffsets, positionsComparator());

            for (final RobotFileInternalElement child : section.getChildren()) {
                final Position position = child.getPosition();
                positionsGroupedByHeaders.get(findHeader(headerOffsets, position)).add(position);
            }

            positions.addAll(toSectionPosition(positionsGroupedByHeaders));

        }
        return positions;
    }

    private Collection<Position> toSectionPosition(final Map<Position, Set<Position>> positionsGroupedByHeaders) {
        final Set<Position> positions = new HashSet<>();
        for (final Position key : positionsGroupedByHeaders.keySet()) {
            if (positionsGroupedByHeaders.get(key).isEmpty()) {
                positions.add(key);
            } else {
                final Position maxPosition = Collections.max(positionsGroupedByHeaders.get(key), positionsComparator());

                final int offset = key.getOffset();
                final int length = maxPosition.getOffset() + maxPosition.getLength() - offset;
                positions.add(new Position(offset, length));
            }

        }
        return positions;
    }

    private static Position findHeader(final List<Position> headerPositions, final Position position) {
        Position previous = null;
        for (final Position headerPosition : headerPositions) {
            if (headerPosition.getOffset() > position.getOffset()) {
                break;
            }
            previous = headerPosition;
        }
        return previous;
    }

    private Collection<Position> calculateSectionChildrenPositions(final RobotSuiteFile model,
            final Class<? extends RobotSuiteFileSection> sectionClass) {
        return model.findSection(sectionClass)
                .map(Stream::of)
                .orElseGet(Stream::empty)
                .flatMap(s -> s.getChildren().stream())
                .map(RobotFileInternalElement::getPosition)
                .collect(toList());
    }

    private Collection<Position> calculateDocumentationFoldingPositions(final RobotSuiteFile model) {
        final Collection<Position> positions = new HashSet<>();
        positions.addAll(calculateSuiteDocumentationFoldingPositions(model));
        positions.addAll(calculateCasesDocumentationFoldingPositions(model));
        positions.addAll(calculateTasksDocumentationFoldingPositions(model));
        positions.addAll(calculateKeywordsDocumentationFoldingPositions(model));
        return positions;
    }

    private Collection<Position> calculateSuiteDocumentationFoldingPositions(final RobotSuiteFile model) {
        final Collection<Position> positions = new HashSet<>();
        final Optional<RobotSettingsSection> settingsSection = model.findSection(RobotSettingsSection.class);
        if (settingsSection.isPresent()) {
            for (final RobotKeywordCall setting : settingsSection.get().getChildren()) {
                if (setting.getLinkedElement().getModelType() == ModelType.SUITE_DOCUMENTATION) {
                    positions.add(setting.getPosition());
                }
            }
        }
        return positions;
    }

    private Collection<Position> calculateCasesDocumentationFoldingPositions(final RobotSuiteFile model) {
        final Collection<Position> positions = new HashSet<>();
        final Optional<RobotCasesSection> casesSection = model.findSection(RobotCasesSection.class);
        if (casesSection.isPresent()) {
            for (final RobotCase test : casesSection.get().getChildren()) {
                for (final RobotKeywordCall call : test.getChildren()) {
                    if (call instanceof RobotDefinitionSetting && ((RobotDefinitionSetting) call).isDocumentation()) {
                        positions.add(call.getPosition());
                    }
                }
            }
        }
        return positions;
    }

    private Collection<Position> calculateTasksDocumentationFoldingPositions(final RobotSuiteFile model) {
        final Collection<Position> positions = new HashSet<>();
        final Optional<RobotTasksSection> tasksSection = model.findSection(RobotTasksSection.class);
        if (tasksSection.isPresent()) {
            for (final RobotTask task : tasksSection.get().getChildren()) {
                for (final RobotKeywordCall call : task.getChildren()) {
                    if (call instanceof RobotDefinitionSetting && ((RobotDefinitionSetting) call).isDocumentation()) {
                        positions.add(call.getPosition());
                    }
                }
            }
        }
        return positions;
    }

    private Collection<Position> calculateKeywordsDocumentationFoldingPositions(final RobotSuiteFile model) {
        final Collection<Position> positions = new HashSet<>();
        final Optional<RobotKeywordsSection> keywordsSection = model.findSection(RobotKeywordsSection.class);
        if (keywordsSection.isPresent()) {
            for (final RobotKeywordDefinition keyword : keywordsSection.get().getChildren()) {
                for (final RobotKeywordCall call : keyword.getChildren()) {
                    if (call instanceof RobotDefinitionSetting && ((RobotDefinitionSetting) call).isDocumentation()) {
                        positions.add(call.getPosition());
                    }
                }
            }
        }
        return positions;
    }

    private static Comparator<Position> positionsComparator() {
        return (p1, p2) -> Integer.compare(p1.getOffset(), p2.getOffset());
    }

    private static Predicate<Position> onlyPositionsSpanning(final IDocument document, final int linesToSpan) {
        return position -> {
            final int startLine = DocumentUtilities.getLine(document, position.getOffset());
            final int endLine;
            if (position.getOffset() + position.getLength() == document.getLength()) {
                endLine = DocumentUtilities.getLine(document, position.getOffset() + position.getLength() - 1);
            } else {
                endLine = DocumentUtilities.getLine(document, position.getOffset() + position.getLength());
            }
            return endLine - startLine + 1 >= linesToSpan;
        };
    }

    private static Function<Position, Position> nextLineShiftedPosition(final IDocument document) {
        return position -> {
            try {
                final int line = document.getLineOfOffset(position.getOffset() + position.getLength());
                final int nextLine = line + 1;
                if (nextLine >= document.getNumberOfLines()) {
                    return new Position(position.getOffset(), Math.max(0, document.getLength() - position.getOffset()));
                } else {
                    final int nextLineOffset = document.getLineInformation(nextLine).getOffset();
                    return new Position(position.getOffset(), nextLineOffset - position.getOffset());
                }
            } catch (final BadLocationException e) {
                return position;
            }
        };
    }

    private void resetFoldingStructure() {
        final Iterator<Annotation> annotationIterator = annotationsModel.getAnnotationIterator();
        while (annotationIterator.hasNext()) {
            final Annotation next = annotationIterator.next();
            if (next instanceof ProjectionAnnotation) {
                annotationsModel.removeAnnotation(next);
            }
        }
    }

    @VisibleForTesting
    void updateFoldingStructure(final Collection<Position> positions) {
        final List<Annotation> annotationsToRemove = new ArrayList<>();
        final HashMap<ProjectionAnnotation, Position> annotationsToAdd = new HashMap<>();
        final List<Annotation> annotationsToChange = new ArrayList<>();

        final Map<Position, Annotation> newFoldingAnnotations = new HashMap<>();
        for (final Position position : positions) {
            if (oldFoldingAnnotations.containsKey(position)) {
                final Annotation annotation = oldFoldingAnnotations.get(position);
                annotationsToChange.add(annotation);
                newFoldingAnnotations.put(new Position(position.getOffset(), position.getLength()), annotation);

            } else {
                final ProjectionAnnotation annotation = new ProjectionAnnotation();
                annotationsToAdd.put(annotation, position);
                newFoldingAnnotations.put(new Position(position.getOffset(), position.getLength()), annotation);
            }
        }
        for (final Position position : oldFoldingAnnotations.keySet()) {
            if (!newFoldingAnnotations.containsKey(position)) {
                annotationsToRemove.add(oldFoldingAnnotations.get(position));
            }
        }

        try {
            int index = 0;
            if (!textControl.isDisposed()) {
                index = textControl.getHorizontalIndex();
                textControl.setRedraw(false);
            }
            annotationsModel.modifyAnnotations(annotationsToRemove.toArray(new Annotation[0]), annotationsToAdd,
                    annotationsToChange.toArray(new Annotation[0]));
            // workaround : without this the horizontal scrollbar is reset to 0 position when
            // writing at the end of long line
            if (!textControl.isDisposed()) {
                textControl.setHorizontalIndex(index);
            }

        } finally {
            if (!textControl.isDisposed()) {
                textControl.setRedraw(true);
            }
            oldFoldingAnnotations = newFoldingAnnotations;
        }
    }
}
