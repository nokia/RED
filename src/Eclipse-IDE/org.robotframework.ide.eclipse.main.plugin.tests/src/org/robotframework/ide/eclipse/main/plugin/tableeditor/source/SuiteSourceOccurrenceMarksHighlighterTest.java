/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.source;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.junit.Rule;
import org.junit.Test;
import org.robotframework.ide.eclipse.main.plugin.mockdocument.Document;
import org.robotframework.ide.eclipse.main.plugin.mockmodel.RobotSuiteFileCreator;
import org.robotframework.red.junit.ShellProvider;

public class SuiteSourceOccurrenceMarksHighlighterTest {

    @Rule
    public ShellProvider shellProvider = new ShellProvider();

    @Test
    public void thereAreNoAnnotations_whenHighlighterIsInstalled() {
        final MockAnnotationModel annotationModel = new MockAnnotationModel();
        final SourceViewer viewer = prepareViewer(annotationModel);

        final IDocument document = new Document("abc  def  ghi");

        final SuiteSourceOccurrenceMarksHighlighter highlighter = new SuiteSourceOccurrenceMarksHighlighter(
                new RobotSuiteFileCreator().build(), document);
        highlighter.install(viewer);

        assertThat(annotationModel.isEmpty()).isTrue();
    }

    @Test
    public void thereIsSingleAnnotation_whenOffsetHitsTheCell_1() {
        final MockAnnotationModel annotationModel = new MockAnnotationModel();
        final SourceViewer viewer = prepareViewer(annotationModel);

        final IDocument document = new Document("abc  def  ghi");

        final SuiteSourceOccurrenceMarksHighlighter highlighter = new SuiteSourceOccurrenceMarksHighlighter(
                new RobotSuiteFileCreator().build(), document);
        highlighter.install(viewer);
        highlighter.refreshOccurrences(1);

        assertThat(annotationModel.getPositions()).containsOnly(new Position(0, 3));
    }

    @Test
    public void thereIsSingleAnnotation_whenOffsetHitsTheCell_2() {
        final MockAnnotationModel annotationModel = new MockAnnotationModel();
        final SourceViewer viewer = prepareViewer(annotationModel);

        final IDocument document = new Document("abc  def  ghi");

        final SuiteSourceOccurrenceMarksHighlighter highlighter = new SuiteSourceOccurrenceMarksHighlighter(
                new RobotSuiteFileCreator().build(), document);
        highlighter.install(viewer);
        highlighter.refreshOccurrences(5);

        assertThat(annotationModel.getPositions()).containsOnly(new Position(5, 3));
    }

    @Test
    public void thereIsSingleAnnotation_whenOffsetHitsTheCell_3() {
        final MockAnnotationModel annotationModel = new MockAnnotationModel();
        final SourceViewer viewer = prepareViewer(annotationModel);

        final IDocument document = new Document("abc  def  ghi");

        final SuiteSourceOccurrenceMarksHighlighter highlighter = new SuiteSourceOccurrenceMarksHighlighter(
                new RobotSuiteFileCreator().build(), document);
        highlighter.install(viewer);
        highlighter.refreshOccurrences(8);

        assertThat(annotationModel.getPositions()).containsOnly(new Position(5, 3));
    }

    @Test
    public void thereAreMultipleAnnotations_whenOffsetHitsTheCellWhichIsFoundElsewhere() {
        final MockAnnotationModel annotationModel = new MockAnnotationModel();
        final SourceViewer viewer = prepareViewer(annotationModel);

        final IDocument document = new Document("abc  def  ghi", "jkl  abc  mno");

        final SuiteSourceOccurrenceMarksHighlighter highlighter = new SuiteSourceOccurrenceMarksHighlighter(
                new RobotSuiteFileCreator().build(), document);
        highlighter.install(viewer);
        highlighter.refreshOccurrences(1);

        assertThat(annotationModel.getPositions()).containsOnly(new Position(0, 3), new Position(19, 3));
    }

    @Test
    public void thereAreNoAnnotations_whenOffsetHitsSeparator() {
        final MockAnnotationModel annotationModel = new MockAnnotationModel();
        final SourceViewer viewer = prepareViewer(annotationModel);

        final IDocument document = new Document("abc  def  ghi");

        final SuiteSourceOccurrenceMarksHighlighter highlighter = new SuiteSourceOccurrenceMarksHighlighter(
                new RobotSuiteFileCreator().build(), document);
        highlighter.install(viewer);
        highlighter.refreshOccurrences(4);

        assertThat(annotationModel.isEmpty()).isTrue();
    }

    @Test
    public void annotationsAreRemoved_whenMovingFromCellToSeparator() {
        final MockAnnotationModel annotationModel = new MockAnnotationModel();
        final SourceViewer viewer = prepareViewer(annotationModel);

        final IDocument document = new Document("abc  def  ghi");

        final SuiteSourceOccurrenceMarksHighlighter highlighter = new SuiteSourceOccurrenceMarksHighlighter(
                new RobotSuiteFileCreator().build(), document);
        highlighter.install(viewer);
        highlighter.refreshOccurrences(2);
        highlighter.refreshOccurrences(4);

        assertThat(annotationModel.isEmpty()).isTrue();
    }

    @Test
    public void annotationsAreAdded_whenMovingFromSeparatorToCell() {
        final MockAnnotationModel annotationModel = new MockAnnotationModel();
        final SourceViewer viewer = prepareViewer(annotationModel);

        final IDocument document = new Document("abc  def  ghi");

        final SuiteSourceOccurrenceMarksHighlighter highlighter = new SuiteSourceOccurrenceMarksHighlighter(
                new RobotSuiteFileCreator().build(), document);
        highlighter.install(viewer);
        highlighter.refreshOccurrences(4);
        highlighter.refreshOccurrences(2);

        assertThat(annotationModel.getPositions()).containsOnly(new Position(0, 3));
    }

    @Test
    public void annotationsStatys_whenMovingWithinCell() {
        final MockAnnotationModel annotationModel = new MockAnnotationModel();
        final SourceViewer viewer = prepareViewer(annotationModel);

        final IDocument document = new Document("abc  def  ghi");

        final SuiteSourceOccurrenceMarksHighlighter highlighter = new SuiteSourceOccurrenceMarksHighlighter(
                new RobotSuiteFileCreator().build(), document);
        highlighter.install(viewer);
        highlighter.refreshOccurrences(1);
        highlighter.refreshOccurrences(2);

        assertThat(annotationModel.getPositions()).containsOnly(new Position(0, 3));
    }

    private SourceViewer prepareViewer(final MockAnnotationModel annotationModel) {
        final StyledText sourceWidget = new StyledText(shellProvider.getShell(), SWT.MULTI);
        final SourceViewer viewer = mock(SourceViewer.class);
        when(viewer.getTextWidget()).thenReturn(sourceWidget);
        when(viewer.getAnnotationModel()).thenReturn(annotationModel);
        return viewer;
    }
}
