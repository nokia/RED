/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.cases.handler;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.robotframework.ide.eclipse.main.plugin.model.ModelConditions.name;
import static org.robotframework.ide.eclipse.main.plugin.model.ModelConditions.noFilePositions;
import static org.robotframework.ide.eclipse.main.plugin.model.ModelConditions.nullParent;

import java.util.List;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.nebula.widgets.nattable.coordinate.PositionCoordinate;
import org.junit.Before;
import org.junit.Test;
import org.robotframework.ide.eclipse.main.plugin.mockeclipse.RedClipboardMock;
import org.robotframework.ide.eclipse.main.plugin.mockmodel.RobotSuiteFileCreator;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCase;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCasesSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.AddingToken;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.AddingToken.TokenState;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotFormEditor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.SelectionLayerAccessor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.cases.handler.CopyInCasesTableHandler.E4CopyInCasesTableHandler;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.dnd.PositionCoordinateTransfer.PositionCoordinateSerializer;

import com.google.common.collect.Lists;

public class CopyInCasesTableHandlerTest {

    private final E4CopyInCasesTableHandler handler = new E4CopyInCasesTableHandler();

    private final RobotFormEditor editor = mock(RobotFormEditor.class);
    private final SelectionLayerAccessor selectionLayerAccessor = mock(SelectionLayerAccessor.class);

    private RedClipboardMock clipboard;

    @Before
    public void beforeTest() {
        clipboard = new RedClipboardMock();
        when(editor.getSelectionLayerAccessor()).thenReturn(selectionLayerAccessor);
    }
    
    @Test
    public void nothingIsCopied_whenNothingIsSelected() {
        when(selectionLayerAccessor.getSelectedPositions()).thenReturn(new PositionCoordinate[] {});
        final IStructuredSelection selection = new StructuredSelection(Lists.<Object> newArrayList());

        final boolean copied = handler.copyContent(editor, selection, clipboard);
        assertThat(copied).isFalse();
        assertThat(clipboard.isEmpty()).isTrue();
    }

    @Test
    public void callsWithPositionsAreCopied_whenOnlyCallsAreSelected() {
        final List<RobotCase> cases = createCases();
        final RobotKeywordCall selectedCall1 = cases.get(0).getChildren().get(0);
        final RobotKeywordCall selectedCall2 = cases.get(0).getChildren().get(2);
        
        final PositionCoordinate[] selectedPositions = new PositionCoordinate[] {
                new PositionCoordinate(null, 1, 1),
                new PositionCoordinate(null, 3, 3)};
        final List<?> selectedElements = newArrayList(selectedCall1, selectedCall2);

        when(selectionLayerAccessor.getSelectedPositions()).thenReturn(selectedPositions);
        when(selectionLayerAccessor.getElementSelectedAt(1)).thenReturn(selectedCall1);
        when(selectionLayerAccessor.getElementSelectedAt(3)).thenReturn(selectedCall2);

        final IStructuredSelection selection = new StructuredSelection(selectedElements);

        final boolean copied = handler.copyContent(editor, selection, clipboard);
        assertThat(copied).isTrue();

        assertThat(clipboard.hasCases()).isFalse();

        assertThat(clipboard.getKeywordCalls()).hasSize(2);
        assertThat(clipboard.getKeywordCalls()[0]).has(nullParent()).has(noFilePositions()).has(name("tags"));
        assertThat(clipboard.getKeywordCalls()[1]).has(nullParent()).has(noFilePositions()).has(name("b"));

        assertThat(clipboard.getPositionsCoordinates()).containsExactly(
                new PositionCoordinateSerializer(1, 1),
                new PositionCoordinateSerializer(3, 3));
    }
    
    @Test
    public void casesWithPositionsAreCopied_whenOnlyCasesAreSelected() {
        final List<RobotCase> cases = createCases();
        final RobotCase selectedCase1 = cases.get(0);
        final RobotCase selectedCase2 = cases.get(1);
        
        final PositionCoordinate[] selectedPositions = new PositionCoordinate[] {
                new PositionCoordinate(null, 0, 0),
                new PositionCoordinate(null, 2, 5) };
        final List<?> selectedElements = newArrayList(selectedCase1, selectedCase2);

        when(selectionLayerAccessor.getSelectedPositions()).thenReturn(selectedPositions);
        when(selectionLayerAccessor.getElementSelectedAt(0)).thenReturn(selectedCase1);
        when(selectionLayerAccessor.getElementSelectedAt(5)).thenReturn(selectedCase2);

        final IStructuredSelection selection = new StructuredSelection(selectedElements);

        final boolean copied = handler.copyContent(editor, selection, clipboard);
        assertThat(copied).isTrue();

        assertThat(clipboard.getCases()).hasSize(2);
        assertThat(clipboard.getCases()[0]).has(nullParent()).has(noFilePositions()).has(name("case 1"));
        assertThat(clipboard.getCases()[1]).has(nullParent()).has(noFilePositions()).has(name("case 2"));

        assertThat(clipboard.hasKeywordCalls()).isFalse();

        assertThat(clipboard.getPositionsCoordinates()).containsExactly(
                new PositionCoordinateSerializer(0, 0),
                new PositionCoordinateSerializer(2, 5));
    }

    @Test
    public void casesAndCallsWithPositionsAreCopied_whenBothAreSelected() {
        final List<RobotCase> cases = createCases();
        final RobotCase selectedCase = cases.get(0);
        final RobotKeywordCall selectedCall = cases.get(0).getChildren().get(1);
        
        final PositionCoordinate[] selectedPositions = new PositionCoordinate[] {
                new PositionCoordinate(null, 0, 0),
                new PositionCoordinate(null, 1, 2) };
        @SuppressWarnings("unchecked")
        final List<?> selectedElements = newArrayList(selectedCase, selectedCall);

        when(selectionLayerAccessor.getSelectedPositions()).thenReturn(selectedPositions);
        when(selectionLayerAccessor.getElementSelectedAt(0)).thenReturn(selectedCase);
        when(selectionLayerAccessor.getElementSelectedAt(2)).thenReturn(selectedCall);

        final IStructuredSelection selection = new StructuredSelection(selectedElements);

        final boolean copied = handler.copyContent(editor, selection, clipboard);
        assertThat(copied).isTrue();

        assertThat(clipboard.getCases()).hasSize(1);
        assertThat(clipboard.getCases()[0]).has(nullParent()).has(noFilePositions()).has(name("case 1"));

        assertThat(clipboard.getKeywordCalls()).hasSize(1);
        assertThat(clipboard.getKeywordCalls()[0]).has(nullParent()).has(noFilePositions()).has(name("a"));

        assertThat(clipboard.getPositionsCoordinates()).containsExactly(
                new PositionCoordinateSerializer(0, 0),
                new PositionCoordinateSerializer(1, 2));
    }

    @Test
    public void nothingIsCopied_whenOnlyAddingTokensAreSelected() {
        final PositionCoordinate[] selectedPositions = new PositionCoordinate[] {
                new PositionCoordinate(null, 0, 0),
                new PositionCoordinate(null, 1, 1)};
        final List<?> selectedElements = newArrayList(
                new AddingToken(null, mock(TokenState.class)),
                new AddingToken(null, mock(TokenState.class)));

        when(selectionLayerAccessor.getSelectedPositions()).thenReturn(selectedPositions);

        final IStructuredSelection selection = new StructuredSelection(selectedElements);

        final boolean copied = handler.copyContent(editor, selection, clipboard);
        assertThat(copied).isFalse();
        assertThat(clipboard.isEmpty()).isTrue();
    }

    @Test
    public void positionsOfAddingTokensAreNotCopied_whenTheyAreSelectedAmongsOtherRobotElements() {
        final List<RobotCase> cases = createCases();
        final RobotCase selectedCase = cases.get(0);
        final RobotKeywordCall selectedCall = cases.get(0).getChildren().get(0);
        final AddingToken selectedToken = new AddingToken(null, mock(TokenState.class));
        
        final PositionCoordinate[] selectedPositions = new PositionCoordinate[] {
                new PositionCoordinate(null, 0, 0),
                new PositionCoordinate(null, 1, 1),
                new PositionCoordinate(null, 2, 4) };
        final List<?> selectedElements = newArrayList(selectedCase, selectedCall, selectedToken);

        when(selectionLayerAccessor.getSelectedPositions()).thenReturn(selectedPositions);
        when(selectionLayerAccessor.getElementSelectedAt(0)).thenReturn(selectedCase);
        when(selectionLayerAccessor.getElementSelectedAt(1)).thenReturn(selectedCall);
        when(selectionLayerAccessor.getElementSelectedAt(4)).thenReturn(selectedToken);

        final IStructuredSelection selection = new StructuredSelection(selectedElements);

        final boolean copied = handler.copyContent(editor, selection, clipboard);
        assertThat(copied).isTrue();

        assertThat(clipboard.getCases()).hasSize(1);
        assertThat(clipboard.getCases()[0]).has(nullParent()).has(noFilePositions()).has(name("case 1"));

        assertThat(clipboard.getKeywordCalls()).hasSize(1);
        assertThat(clipboard.getKeywordCalls()[0]).has(nullParent()).has(noFilePositions()).has(name("tags"));

        assertThat(clipboard.getPositionsCoordinates()).containsExactly(
                new PositionCoordinateSerializer(0, 0),
                new PositionCoordinateSerializer(1, 1));
    }

    private static List<RobotCase> createCases() {
        final RobotSuiteFile model = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("case 1")
                .appendLine("  [tags]  tag1")
                .appendLine("  a  1")
                .appendLine("  b  2")
                .appendLine("case 2")
                .appendLine("  [tags]  tag2")
                .appendLine("  c  3")
                .appendLine("  d  4")
                .build();
        return model.findSection(RobotCasesSection.class).get().getChildren();
    }
}
