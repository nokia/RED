package org.robotframework.ide.eclipse.main.plugin.propertytester;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.eclipse.nebula.widgets.nattable.coordinate.PositionCoordinate;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotFormEditor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.SelectionLayerAccessor;

public class TableCellPropertyTesterTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private final TableCellPropertyTester tester = new TableCellPropertyTester();

    @Test
    public void exceptionIsThrown_whenReceiverIsNotStructuredSelection() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Property tester is unable to test properties of java.lang.Object. It should be used with "
                + RobotFormEditor.class.getName());

        tester.test(new Object(), "property", null, true);
    }

    @Test
    public void falseIsReturned_whenExpectedValueIsAString() {
        final RobotFormEditor editor = mock(RobotFormEditor.class);
        final boolean testResult = tester.test(editor, "unknown_property", null, "value");

        assertThat(testResult).isFalse();
    }

    @Test
    public void falseIsReturned_forUnknownProperty() {
        final RobotFormEditor editor = mock(RobotFormEditor.class);
        assertThat(tester.test(editor, "unknown_property", null, true)).isFalse();
        assertThat(tester.test(editor, "unknown_property", null, false)).isFalse();
    }

    @Test
    public void testOnlyFullRowsAreSelectedProperty() {
        final RobotFormEditor editor = mock(RobotFormEditor.class);
        when(editor.getSelectionLayerAccessor()).thenReturn(null);

        assertThat(onlyFullRowsAreSelected(editor, false)).isFalse();
        assertThat(onlyFullRowsAreSelected(editor, true)).isFalse();

        final SelectionLayerAccessor sla = mock(SelectionLayerAccessor.class);
        when(editor.getSelectionLayerAccessor()).thenReturn(sla);
        when(sla.onlyFullRowsAreSelected()).thenReturn(true);

        assertThat(onlyFullRowsAreSelected(editor, false)).isFalse();
        assertThat(onlyFullRowsAreSelected(editor, true)).isTrue();

        when(sla.onlyFullRowsAreSelected()).thenReturn(false);

        assertThat(onlyFullRowsAreSelected(editor, false)).isTrue();
        assertThat(onlyFullRowsAreSelected(editor, true)).isFalse();
    }

    private boolean onlyFullRowsAreSelected(final RobotFormEditor editor, final boolean expected) {
        return tester.test(editor, "onlyFullRowsAreSelected", null, expected);
    }

    @Test
    public void testNoFullRowIsSelectedProperty() {
        final RobotFormEditor editor = mock(RobotFormEditor.class);
        when(editor.getSelectionLayerAccessor()).thenReturn(null);

        assertThat(noFullRowIsSelected(editor, false)).isFalse();
        assertThat(noFullRowIsSelected(editor, true)).isFalse();

        final SelectionLayerAccessor sla = mock(SelectionLayerAccessor.class);
        when(editor.getSelectionLayerAccessor()).thenReturn(sla);
        when(sla.noFullRowIsSelected()).thenReturn(true);

        assertThat(noFullRowIsSelected(editor, false)).isFalse();
        assertThat(noFullRowIsSelected(editor, true)).isTrue();

        when(sla.noFullRowIsSelected()).thenReturn(false);

        assertThat(noFullRowIsSelected(editor, false)).isTrue();
        assertThat(noFullRowIsSelected(editor, true)).isFalse();
    }

    private boolean noFullRowIsSelected(final RobotFormEditor editor, final boolean expected) {
        return tester.test(editor, "noFullRowIsSelected", null, expected);
    }

    @Test
    public void testNumberOfSelectedCellEqualsProperty() {
        final RobotFormEditor editor = mock(RobotFormEditor.class);
        when(editor.getSelectionLayerAccessor()).thenReturn(null);

        assertThat(numberOfSelectedCellEquals(editor, 0)).isFalse();
        assertThat(numberOfSelectedCellEquals(editor, 1)).isFalse();
        assertThat(numberOfSelectedCellEquals(editor, 2)).isFalse();

        final SelectionLayerAccessor sla = mock(SelectionLayerAccessor.class);
        when(editor.getSelectionLayerAccessor()).thenReturn(sla);
        final PositionCoordinate[] coordinates = new PositionCoordinate[1];
        when(sla.getSelectedPositions()).thenReturn(coordinates);

        assertThat(numberOfSelectedCellEquals(editor, 0)).isFalse();
        assertThat(numberOfSelectedCellEquals(editor, 1)).isTrue();
        assertThat(numberOfSelectedCellEquals(editor, 2)).isFalse();
    }

    private boolean numberOfSelectedCellEquals(final RobotFormEditor editor, final int expected) {
        return tester.test(editor, "numberOfSelectedCellEquals", null, expected);
    }

    @Test
    public void testIsAnyCellSelectedInColumnProperty() {
        final RobotFormEditor editor = mock(RobotFormEditor.class);
        when(editor.getSelectionLayerAccessor()).thenReturn(null);

        assertThat(isAnyCellSelectedInColumn(editor, 0)).isFalse();
        assertThat(isAnyCellSelectedInColumn(editor, 1)).isFalse();
        assertThat(isAnyCellSelectedInColumn(editor, 2)).isFalse();

        final SelectionLayerAccessor sla = mock(SelectionLayerAccessor.class);
        when(editor.getSelectionLayerAccessor()).thenReturn(sla);
        when(sla.isAnyCellSelectedInColumn(1)).thenReturn(true);

        assertThat(isAnyCellSelectedInColumn(editor, 0)).isFalse();
        assertThat(isAnyCellSelectedInColumn(editor, 1)).isTrue();
        assertThat(isAnyCellSelectedInColumn(editor, 2)).isFalse();
    }

    private boolean isAnyCellSelectedInColumn(final RobotFormEditor editor, final int expected) {
        return tester.test(editor, "isAnyCellSelectedInColumn", null, expected);
    }
}
