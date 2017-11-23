/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.source;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Map;

import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.projection.ProjectionAnnotationModel;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.robotframework.ide.eclipse.main.plugin.RedPreferences;
import org.robotframework.ide.eclipse.main.plugin.RedPreferences.FoldableElements;
import org.robotframework.ide.eclipse.main.plugin.mockdocument.Document;
import org.robotframework.ide.eclipse.main.plugin.mockmodel.RobotSuiteFileCreator;
import org.robotframework.red.junit.ShellProvider;
import org.robotframework.red.swt.StyledTextWrapper;

public class SuiteSourceEditorFoldingSupportTest {

    @Rule
    public ShellProvider shellProvider = new ShellProvider();

    @Test
    public void sectionsPositionsAreProperlyCalculated() {
        final RedPreferences preferences = mock(RedPreferences.class);
        when(preferences.getFoldingLineLimit()).thenReturn(1);
        when(preferences.getFoldableElements()).thenReturn(EnumSet.of(FoldableElements.SECTIONS));

        final RobotSuiteFileCreator builder = createFileBuilder();
        final SuiteSourceEditorFoldingSupport support = new SuiteSourceEditorFoldingSupport(preferences, null, null);

        final Collection<Position> foldingPositions = support.calculateFoldingPositions(builder.build(),
                new Document(builder.getContent()));

        assertThat(foldingPositions).containsOnly(new Position(0, 17), new Position(17, 18), new Position(35, 19),
                new Position(54, 17), new Position(71, 30), new Position(101, 28), new Position(129, 111),
                new Position(240, 103), new Position(343, 76), new Position(419, 44));
    }

    @Test
    public void testCasesPositionsAreProperlyCalculated() {
        final RedPreferences preferences = mock(RedPreferences.class);
        when(preferences.getFoldingLineLimit()).thenReturn(1);
        when(preferences.getFoldableElements()).thenReturn(EnumSet.of(FoldableElements.CASES));

        final RobotSuiteFileCreator builder = createFileBuilder();
        final SuiteSourceEditorFoldingSupport support = new SuiteSourceEditorFoldingSupport(preferences, null, null);

        final Collection<Position> foldingPositions = support.calculateFoldingPositions(builder.build(),
                new Document(builder.getContent()));

        assertThat(foldingPositions).containsOnly(new Position(148, 7), new Position(155, 32), new Position(187, 53));
    }

    @Test
    public void keywordsPositionsAreProperlyCalculated() {
        final RedPreferences preferences = mock(RedPreferences.class);
        when(preferences.getFoldingLineLimit()).thenReturn(1);
        when(preferences.getFoldableElements()).thenReturn(EnumSet.of(FoldableElements.KEYWORDS));

        final RobotSuiteFileCreator builder = createFileBuilder();
        final SuiteSourceEditorFoldingSupport support = new SuiteSourceEditorFoldingSupport(preferences, null, null);

        final Collection<Position> foldingPositions = support.calculateFoldingPositions(builder.build(),
                new Document(builder.getContent()));

        assertThat(foldingPositions).containsOnly(new Position(257, 5), new Position(262, 30), new Position(292, 51));
    }

    @Test
    public void documentationPositionsAreProperlyCalculated() {
        final RedPreferences preferences = mock(RedPreferences.class);
        when(preferences.getFoldingLineLimit()).thenReturn(1);
        when(preferences.getFoldableElements()).thenReturn(EnumSet.of(FoldableElements.DOCUMENTATION));

        final RobotSuiteFileCreator builder = createFileBuilder();
        final SuiteSourceEditorFoldingSupport support = new SuiteSourceEditorFoldingSupport(preferences, null, null);

        final Collection<Position> foldingPositions = support.calculateFoldingPositions(builder.build(),
                new Document(builder.getContent()));

        assertThat(foldingPositions).containsOnly(new Position(164, 23), new Position(196, 44), new Position(269, 23),
                new Position(299, 44), new Position(360, 36));
    }

    @Test
    public void positionsAreProperlyCalculatedWithChangedLineSpan() {
        final RedPreferences preferences = mock(RedPreferences.class);
        when(preferences.getFoldingLineLimit()).thenReturn(3);
        when(preferences.getFoldableElements()).thenReturn(EnumSet.allOf(FoldableElements.class));

        final RobotSuiteFileCreator builder = createFileBuilder();
        final SuiteSourceEditorFoldingSupport support = new SuiteSourceEditorFoldingSupport(preferences, null, null);

        final Collection<Position> foldingPositions = support.calculateFoldingPositions(builder.build(),
                new Document(builder.getContent()));

        assertThat(foldingPositions).containsOnly(new Position(129, 111), new Position(240, 103), new Position(343, 76),
                new Position(419, 44), new Position(187, 53), new Position(292, 51));
    }

    @Test
    public void allAnnotationsAreAddedInitially() {
        final StyledTextWrapper textControl = new StyledTextWrapper(new StyledText(shellProvider.getShell(), SWT.NONE));
        final ProjectionAnnotationModel annotationsModel = mock(ProjectionAnnotationModel.class);

        final SuiteSourceEditorFoldingSupport support = new SuiteSourceEditorFoldingSupport(textControl,
                annotationsModel);

        support.updateFoldingStructure(newArrayList(new Position(0, 10), new Position(20, 10)));

        verify(annotationsModel, times(1)).modifyAnnotations(arrayOfSize(0),
                SuiteSourceEditorFoldingSupportTest.<Annotation, Position> mapOfSize(2), arrayOfSize(0));
        verifyNoMoreInteractions(annotationsModel);
    }

    @Test
    public void whenNoFoldingPositionAppearsOrDissappears_theAnnotationsAreSendAsChanged() {
        final StyledTextWrapper textControl = new StyledTextWrapper(new StyledText(shellProvider.getShell(), SWT.NONE));
        final ProjectionAnnotationModel annotationsModel = mock(ProjectionAnnotationModel.class);

        final SuiteSourceEditorFoldingSupport support = new SuiteSourceEditorFoldingSupport(textControl,
                annotationsModel);

        support.updateFoldingStructure(newArrayList(new Position(0, 10), new Position(20, 10)));
        support.updateFoldingStructure(newArrayList(new Position(0, 10), new Position(20, 10)));

        verify(annotationsModel, times(1)).modifyAnnotations(arrayOfSize(0),
                SuiteSourceEditorFoldingSupportTest.<Annotation, Position> mapOfSize(2), arrayOfSize(0));
        verify(annotationsModel, times(1)).modifyAnnotations(arrayOfSize(0),
                SuiteSourceEditorFoldingSupportTest.<Annotation, Position> mapOfSize(0), arrayOfSize(2));
        verifyNoMoreInteractions(annotationsModel);
    }

    @Test
    public void whenNewFoldingPositionAppears_itIsSendAsAddedWhileOtherAreChanged() {
        final StyledTextWrapper textControl = new StyledTextWrapper(new StyledText(shellProvider.getShell(), SWT.NONE));
        final ProjectionAnnotationModel annotationsModel = mock(ProjectionAnnotationModel.class);

        final SuiteSourceEditorFoldingSupport support = new SuiteSourceEditorFoldingSupport(textControl,
                annotationsModel);

        support.updateFoldingStructure(newArrayList(new Position(0, 10), new Position(20, 10)));
        support.updateFoldingStructure(newArrayList(new Position(0, 10), new Position(20, 10), new Position(30, 10)));

        verify(annotationsModel, times(1)).modifyAnnotations(arrayOfSize(0),
                SuiteSourceEditorFoldingSupportTest.<Annotation, Position> mapOfSize(2), arrayOfSize(0));
        verify(annotationsModel, times(1)).modifyAnnotations(arrayOfSize(0),
                SuiteSourceEditorFoldingSupportTest.<Annotation, Position> mapOfSize(1), arrayOfSize(2));
        verifyNoMoreInteractions(annotationsModel);
    }

    @Test
    public void whenFoldingPositionDissappears_itIsSendAsRemovedWhileOtherAreChanged() {
        final StyledTextWrapper textControl = new StyledTextWrapper(new StyledText(shellProvider.getShell(), SWT.NONE));
        final ProjectionAnnotationModel annotationsModel = mock(ProjectionAnnotationModel.class);

        final SuiteSourceEditorFoldingSupport support = new SuiteSourceEditorFoldingSupport(textControl,
                annotationsModel);

        support.updateFoldingStructure(newArrayList(new Position(0, 10), new Position(20, 10)));
        support.updateFoldingStructure(newArrayList(new Position(0, 10)));

        verify(annotationsModel, times(1)).modifyAnnotations(arrayOfSize(0),
                SuiteSourceEditorFoldingSupportTest.<Annotation, Position> mapOfSize(2), arrayOfSize(0));
        verify(annotationsModel, times(1)).modifyAnnotations(arrayOfSize(1),
                SuiteSourceEditorFoldingSupportTest.<Annotation, Position> mapOfSize(0), arrayOfSize(1));
        verifyNoMoreInteractions(annotationsModel);
    }

    @Test
    public void whenFoldingPositionIsChanged_ItIsRemovedAndNewIsAdded() {
        final StyledTextWrapper textControl = new StyledTextWrapper(new StyledText(shellProvider.getShell(), SWT.NONE));
        final ProjectionAnnotationModel annotationsModel = mock(ProjectionAnnotationModel.class);

        final SuiteSourceEditorFoldingSupport support = new SuiteSourceEditorFoldingSupport(textControl,
                annotationsModel);

        // this simulates situation in which the position is being modified by some source change listeners
        // deep in eclipse. In such situation the Position objects gets modified; but we're using them
        // as keys in map thus resulting in objects lying in wrong map bucket after modification is done
        final Position positionToModify = new Position(20, 10);
        support.updateFoldingStructure(newArrayList(new Position(0, 10), positionToModify));
        positionToModify.setLength(15);
        support.updateFoldingStructure(newArrayList(new Position(0, 10), new Position(20, 15)));

        verify(annotationsModel, times(1)).modifyAnnotations(arrayOfSize(0),
                SuiteSourceEditorFoldingSupportTest.<Annotation, Position> mapOfSize(2), arrayOfSize(0));
        verify(annotationsModel, times(1)).modifyAnnotations(arrayOfSize(1),
                SuiteSourceEditorFoldingSupportTest.<Annotation, Position> mapOfSize(1), arrayOfSize(1));
        verifyNoMoreInteractions(annotationsModel);
    }

    @Test
    public void textViewerRevealsSelection_whenAnnotationModelIsUpdated() {
        final StyledTextWrapper textControl = spy(
                new StyledTextWrapper(new StyledText(shellProvider.getShell(), SWT.NONE)));
        final ProjectionAnnotationModel annotationsModel = mock(ProjectionAnnotationModel.class);

        final SuiteSourceEditorFoldingSupport support = new SuiteSourceEditorFoldingSupport(textControl,
                annotationsModel);
        support.updateFoldingStructure(newArrayList(new Position(0, 10), new Position(20, 10)));

        final int idx = verify(textControl).getHorizontalIndex();
        verify(textControl).setHorizontalIndex(idx);
    }

    private static RobotSuiteFileCreator createFileBuilder() {
        return new RobotSuiteFileCreator().appendLine("*** Settings ***")
                .appendLine("*** Variables ***")
                .appendLine("*** Test Cases ***")
                .appendLine("*** Keywords ***")
                .appendLine("*** Settings ***")
                .appendLine("Library  lib")
                .appendLine("*** Variables ***")
                .appendLine("${var}  1")
                .appendLine("*** Test Cases ***")
                .appendLine("case 1")
                .appendLine("case 2")
                .appendLine("  [Documentation]  doc 1")
                .appendLine("case 3")
                .appendLine("  [Documentation]  doc 1")
                .appendLine("  ...  doc 2   doc 3")
                .appendLine("*** Keywords ***")
                .appendLine("kw 1")
                .appendLine("kw 2")
                .appendLine("  [Documentation]  doc 1")
                .appendLine("kw 3")
                .appendLine("  [Documentation]  doc 1")
                .appendLine("  ...  doc 2   doc 3")
                .appendLine("*** Settings ***")
                .appendLine("Documentation  doc1")
                .appendLine("...  doc2  doc3")
                .appendLine("Metadata  data1  data2")
                .appendLine("*** Variables ***")
                .appendLine("${var}  1")
                .appendLine("@{list}  a  b  c");
    }

    private static <T1, T2> Map<T1, T2> mapOfSize(final int expectedSize) {
        return argThat(new MapOfSizeMatcher<Map<T1, T2>>(expectedSize));
    }

    private static Annotation[] arrayOfSize(final int expectedSize) {
        return argThat(new ArrayOfSizeMatcher<Annotation[]>(expectedSize));
    }

    private static class ArrayOfSizeMatcher<T> implements ArgumentMatcher<T> {

        private final int expectedSize;

        public ArrayOfSizeMatcher(final int expectedSize) {
            this.expectedSize = expectedSize;
        }

        @Override
        public boolean matches(final T item) {
            return item != null && item.getClass().isArray() && ((Object[]) item).length == expectedSize;
        }
    }

    private static class MapOfSizeMatcher<T> implements ArgumentMatcher<T> {

        private final int expectedSize;

        public MapOfSizeMatcher(final int expectedSize) {
            this.expectedSize = expectedSize;
        }

        @Override
        public boolean matches(final T item) {
            return item instanceof Map<?, ?> && ((Map<?, ?>) item).size() == expectedSize;
        }
    }
}
