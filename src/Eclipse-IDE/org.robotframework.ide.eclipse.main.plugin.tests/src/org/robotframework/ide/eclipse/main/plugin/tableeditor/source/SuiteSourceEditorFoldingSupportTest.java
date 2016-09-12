/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.source;

import static com.google.common.collect.Lists.newArrayList;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.Map;

import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.projection.ProjectionAnnotationModel;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.Rule;
import org.junit.Test;
import org.robotframework.red.junit.ShellProvider;

public class SuiteSourceEditorFoldingSupportTest {

    @Rule
    public ShellProvider shellProvider = new ShellProvider();

    @Test
    public void allAnnotationsAreAddedInitially() {
        final StyledText textControl =  new StyledText(shellProvider.getShell(), SWT.NONE);
        final ProjectionAnnotationModel annotationsModel = mock(ProjectionAnnotationModel.class);

        final SuiteSourceEditorFoldingSupport support = new SuiteSourceEditorFoldingSupport(textControl,
                annotationsModel);

        support.updateFoldingStructure(newArrayList(new Position(0, 10), new Position(20, 10)));
        
        verify(annotationsModel, times(1)).modifyAnnotations(arrayOfSize(0), mapOfSize(2),
                arrayOfSize(0));
        verifyNoMoreInteractions(annotationsModel);
    }

    @Test
    public void whenNoFoldingPositionAppearsOrDissappears_theAnnotationsAreSendAsChanged() {
        final StyledText textControl = new StyledText(shellProvider.getShell(), SWT.NONE);
        final ProjectionAnnotationModel annotationsModel = mock(ProjectionAnnotationModel.class);

        final SuiteSourceEditorFoldingSupport support = new SuiteSourceEditorFoldingSupport(textControl,
                annotationsModel);

        support.updateFoldingStructure(newArrayList(new Position(0, 10), new Position(20, 10)));
        support.updateFoldingStructure(newArrayList(new Position(0, 10), new Position(20, 10)));

        verify(annotationsModel, times(1)).modifyAnnotations(arrayOfSize(0), mapOfSize(2), arrayOfSize(0));
        verify(annotationsModel, times(1)).modifyAnnotations(arrayOfSize(0), mapOfSize(0), arrayOfSize(2));
        verifyNoMoreInteractions(annotationsModel);
    }

    @Test
    public void whenNewFoldingPositionAppears_itIsSendAsAddedWhileOtherAreChanged() {
        final StyledText textControl = new StyledText(shellProvider.getShell(), SWT.NONE);
        final ProjectionAnnotationModel annotationsModel = mock(ProjectionAnnotationModel.class);

        final SuiteSourceEditorFoldingSupport support = new SuiteSourceEditorFoldingSupport(textControl,
                annotationsModel);

        support.updateFoldingStructure(newArrayList(new Position(0, 10), new Position(20, 10)));
        support.updateFoldingStructure(newArrayList(new Position(0, 10), new Position(20, 10), new Position(30, 10)));

        verify(annotationsModel, times(1)).modifyAnnotations(arrayOfSize(0), mapOfSize(2), arrayOfSize(0));
        verify(annotationsModel, times(1)).modifyAnnotations(arrayOfSize(0), mapOfSize(1), arrayOfSize(2));
        verifyNoMoreInteractions(annotationsModel);
    }

    @Test
    public void whenFoldingPositionDissappears_itIsSendAsRemovedWhileOtherAreChanged() {
        final StyledText textControl = new StyledText(shellProvider.getShell(), SWT.NONE);
        final ProjectionAnnotationModel annotationsModel = mock(ProjectionAnnotationModel.class);

        final SuiteSourceEditorFoldingSupport support = new SuiteSourceEditorFoldingSupport(textControl,
                annotationsModel);

        support.updateFoldingStructure(newArrayList(new Position(0, 10), new Position(20, 10)));
        support.updateFoldingStructure(newArrayList(new Position(0, 10)));

        verify(annotationsModel, times(1)).modifyAnnotations(arrayOfSize(0), mapOfSize(2), arrayOfSize(0));
        verify(annotationsModel, times(1)).modifyAnnotations(arrayOfSize(1), mapOfSize(0), arrayOfSize(1));
        verifyNoMoreInteractions(annotationsModel);
    }

    @Test
    public void textViewerRevealsSelection_whenAnnotationModelIsUpdated() {
        final StyledText textControl = spy(new StyledText(shellProvider.getShell(), SWT.NONE));
        final ProjectionAnnotationModel annotationsModel = mock(ProjectionAnnotationModel.class);

        final SuiteSourceEditorFoldingSupport support = new SuiteSourceEditorFoldingSupport(textControl,
                annotationsModel);
        support.updateFoldingStructure(newArrayList(new Position(0, 10), new Position(20, 10)));

        verify(textControl, times(1)).showSelection();
    }

    @SuppressWarnings("rawtypes")
    private static Map mapOfSize(final int expectedSize) {
        return argThat(new MapOfSizeMatcher<Map>(expectedSize));
    }

    private static Annotation[] arrayOfSize(final int expectedSize) {
        return argThat(new ArrayOfSizeMatcher<Annotation[]>(expectedSize));
    }

    private static class ArrayOfSizeMatcher<T> extends BaseMatcher<T> {

        private final int expectedSize;

        public ArrayOfSizeMatcher(final int expectedSize) {
            this.expectedSize = expectedSize;
        }

        @Override
        public boolean matches(final Object item) {
            return item != null && item.getClass().isArray() && ((Object[]) item).length == expectedSize;
        }

        @Override
        public void describeTo(final Description description) {
            // implement when needded
        }
    }

    private static class MapOfSizeMatcher<T> extends BaseMatcher<T> {

        private final int expectedSize;

        public MapOfSizeMatcher(final int expectedSize) {
            this.expectedSize = expectedSize;
        }

        @Override
        public boolean matches(final Object item) {
            return item instanceof Map<?, ?> && ((Map<?, ?>) item).size() == expectedSize;
        }

        @Override
        public void describeTo(final Description description) {
            // implement when needded
        }
    }
}
