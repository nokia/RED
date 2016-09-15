/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.keywords.handler;

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
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordDefinition;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordsSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.AddingToken;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.AddingToken.TokenState;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotFormEditor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.SelectionLayerAccessor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.dnd.PositionCoordinateTransfer.PositionCoordinateSerializer;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.keywords.handler.CopyInKeywordTableHandler.E4CopyInKeywordTableHandler;

import com.google.common.collect.Lists;

public class CopyInKeywordTableHandlerTest {

    private final E4CopyInKeywordTableHandler handler = new E4CopyInKeywordTableHandler();

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
        final List<RobotKeywordDefinition> keywords = createKeywords();
        final RobotKeywordCall selectedCall1 = keywords.get(0).getChildren().get(0);
        final RobotKeywordCall selectedCall2 = keywords.get(0).getChildren().get(2);
        
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

        assertThat(clipboard.hasKeywordDefinitions()).isFalse();

        assertThat(clipboard.getKeywordCalls()).hasSize(2);
        assertThat(clipboard.getKeywordCalls()[0]).has(nullParent()).has(noFilePositions()).has(name("tags"));
        assertThat(clipboard.getKeywordCalls()[1]).has(nullParent()).has(noFilePositions()).has(name("b"));

        assertThat(clipboard.getPositionsCoordinates()).containsExactly(
                new PositionCoordinateSerializer(1, 1),
                new PositionCoordinateSerializer(3, 3));
    }
    
    @Test
    public void keywordsWithPositionsAreCopied_whenOnlyCasesAreSelected() {
        final List<RobotKeywordDefinition> keywords = createKeywords();
        final RobotKeywordDefinition selectedKeyword1 = keywords.get(0);
        final RobotKeywordDefinition selectedKeyword2 = keywords.get(1);
        
        final PositionCoordinate[] selectedPositions = new PositionCoordinate[] {
                new PositionCoordinate(null, 0, 0),
                new PositionCoordinate(null, 2, 5) };
        final List<?> selectedElements = newArrayList(selectedKeyword1, selectedKeyword2);

        when(selectionLayerAccessor.getSelectedPositions()).thenReturn(selectedPositions);
        when(selectionLayerAccessor.getElementSelectedAt(0)).thenReturn(selectedKeyword1);
        when(selectionLayerAccessor.getElementSelectedAt(5)).thenReturn(selectedKeyword2);

        final IStructuredSelection selection = new StructuredSelection(selectedElements);

        final boolean copied = handler.copyContent(editor, selection, clipboard);
        assertThat(copied).isTrue();

        assertThat(clipboard.getKeywordDefinitions()).hasSize(2);
        assertThat(clipboard.getKeywordDefinitions()[0]).has(nullParent()).has(noFilePositions()).has(name("kw 1"));
        assertThat(clipboard.getKeywordDefinitions()[1]).has(nullParent()).has(noFilePositions()).has(name("kw 2"));

        assertThat(clipboard.hasKeywordCalls()).isFalse();

        assertThat(clipboard.getPositionsCoordinates()).containsExactly(
                new PositionCoordinateSerializer(0, 0),
                new PositionCoordinateSerializer(2, 5));
    }

    @Test
    public void keywordsAndCallsWithPositionsAreCopied_whenBothAreSelected() {
        final List<RobotKeywordDefinition> keywords = createKeywords();
        final RobotKeywordDefinition selectedKeyword = keywords.get(0);
        final RobotKeywordCall selectedCall = keywords.get(0).getChildren().get(1);
        
        final PositionCoordinate[] selectedPositions = new PositionCoordinate[] {
                new PositionCoordinate(null, 0, 0),
                new PositionCoordinate(null, 1, 2) };
        @SuppressWarnings("unchecked")
        final List<?> selectedElements = newArrayList(selectedKeyword, selectedCall);

        when(selectionLayerAccessor.getSelectedPositions()).thenReturn(selectedPositions);
        when(selectionLayerAccessor.getElementSelectedAt(0)).thenReturn(selectedKeyword);
        when(selectionLayerAccessor.getElementSelectedAt(2)).thenReturn(selectedCall);

        final IStructuredSelection selection = new StructuredSelection(selectedElements);

        final boolean copied = handler.copyContent(editor, selection, clipboard);
        assertThat(copied).isTrue();

        assertThat(clipboard.getKeywordDefinitions()).hasSize(1);
        assertThat(clipboard.getKeywordDefinitions()[0]).has(nullParent()).has(noFilePositions()).has(name("kw 1"));

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
        final List<RobotKeywordDefinition> keywords = createKeywords();
        final RobotKeywordDefinition selectedKeyword = keywords.get(0);
        final RobotKeywordCall selectedCall = keywords.get(0).getChildren().get(0);
        final AddingToken selectedToken = new AddingToken(null, mock(TokenState.class));
        
        final PositionCoordinate[] selectedPositions = new PositionCoordinate[] {
                new PositionCoordinate(null, 0, 0),
                new PositionCoordinate(null, 1, 1),
                new PositionCoordinate(null, 2, 4) };
        final List<?> selectedElements = newArrayList(selectedKeyword, selectedCall, selectedToken);

        when(selectionLayerAccessor.getSelectedPositions()).thenReturn(selectedPositions);
        when(selectionLayerAccessor.getElementSelectedAt(0)).thenReturn(selectedKeyword);
        when(selectionLayerAccessor.getElementSelectedAt(1)).thenReturn(selectedCall);
        when(selectionLayerAccessor.getElementSelectedAt(4)).thenReturn(selectedToken);

        final IStructuredSelection selection = new StructuredSelection(selectedElements);

        final boolean copied = handler.copyContent(editor, selection, clipboard);
        assertThat(copied).isTrue();

        assertThat(clipboard.getKeywordDefinitions()).hasSize(1);
        assertThat(clipboard.getKeywordDefinitions()[0]).has(nullParent()).has(noFilePositions()).has(name("kw 1"));

        assertThat(clipboard.getKeywordCalls()).hasSize(1);
        assertThat(clipboard.getKeywordCalls()[0]).has(nullParent()).has(noFilePositions()).has(name("tags"));

        assertThat(clipboard.getPositionsCoordinates()).containsExactly(
                new PositionCoordinateSerializer(0, 0),
                new PositionCoordinateSerializer(1, 1));
    }

    private static List<RobotKeywordDefinition> createKeywords() {
        final RobotSuiteFile model = new RobotSuiteFileCreator().appendLine("*** Keywords ***")
                .appendLine("kw 1")
                .appendLine("  [tags]  tag1")
                .appendLine("  a  1")
                .appendLine("  b  2")
                .appendLine("kw 2")
                .appendLine("  [tags]  tag2")
                .appendLine("  c  3")
                .appendLine("  d  4")
                .build();
        return model.findSection(RobotKeywordsSection.class).get().getChildren();
    }
}
