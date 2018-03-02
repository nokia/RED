package org.robotframework.ide.eclipse.main.plugin.tableeditor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.eclipse.nebula.widgets.nattable.data.IRowDataProvider;
import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.junit.Test;
import org.robotframework.ide.eclipse.main.plugin.mockmodel.RobotSuiteFileCreator;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.model.RobotVariable;
import org.robotframework.ide.eclipse.main.plugin.model.RobotVariablesSection;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.ProblemCategory.Severity;

@SuppressWarnings("unchecked")
public class MarkersLabelAccumulatorTest {

    @Test
    public void noLabelsAreAddedForNonModelElements() {
        final IRowDataProvider<String> dataProvider = mock(IRowDataProvider.class);
        when(dataProvider.getRowObject(0)).thenReturn("abc");

        final SuiteFileMarkersContainer markersContainer = new SuiteFileMarkersContainerMock();

        final MarkersLabelAccumulator accumulator = new MarkersLabelAccumulator(markersContainer, dataProvider);

        for (int col = 0; col < 5; col++) {
            final LabelStack configLabels = new LabelStack();
            accumulator.accumulateConfigLabels(configLabels, col, 0);
            assertThat(configLabels.getLabels()).isEmpty();
        }
    }

    @Test
    public void noLabelsAreAddedForElementWithoutMarkers() {
        final IRowDataProvider<RobotVariable> dataProvider = mock(IRowDataProvider.class);

        final RobotVariable variableWithoutMarkers = createVariable();

        final SuiteFileMarkersContainerMock markersContainer = new SuiteFileMarkersContainerMock();
        when(dataProvider.getRowObject(0)).thenReturn(variableWithoutMarkers);

        final MarkersLabelAccumulator accumulator = new MarkersLabelAccumulator(markersContainer, dataProvider);
        for (int col = 0; col < 5; col++) {
            final LabelStack configLabels = new LabelStack();
            accumulator.accumulateConfigLabels(configLabels, col, 0);
            assertThat(configLabels.getLabels()).isEmpty();
        }
    }

    @Test
    public void taskLabelsAreAddedForElementsWithTask() {
        final IRowDataProvider<RobotVariable> dataProvider = mock(IRowDataProvider.class);

        final RobotVariable variable = createVariable();

        final SuiteFileMarkersContainerMock markersContainer = new SuiteFileMarkersContainerMock();
        markersContainer.registerMarkerTask(variable);

        when(dataProvider.getRowObject(0)).thenReturn(variable);

        final MarkersLabelAccumulator accumulator = new MarkersLabelAccumulator(markersContainer, dataProvider);
        for (int col = 0; col < 5; col++) {
            final LabelStack configLabels = new LabelStack();
            accumulator.accumulateConfigLabels(configLabels, col, 0);
            assertThat(configLabels.getLabels()).containsExactly(MarkersLabelAccumulator.TASK_LABEL);
        }
    }

    @Test
    public void infoLabelsAreAddedForElementWithInfoMarker() {
        testLabelAccumulating(Severity.INFO, MarkersLabelAccumulator.INFO_MARKER_LABEL);
    }

    @Test
    public void warningLabelsAreAddedForElementWithWarningMarker() {
        testLabelAccumulating(Severity.WARNING, MarkersLabelAccumulator.WARNING_MARKER_LABEL);
    }

    @Test
    public void errorLabelsAreAddedForElementWithErrorMarker() {
        testLabelAccumulating(Severity.ERROR, MarkersLabelAccumulator.ERROR_MARKER_LABEL);
    }

    private void testLabelAccumulating(final Severity severityOfMarker, final String expectedLabel) {
        final IRowDataProvider<RobotVariable> dataProvider = mock(IRowDataProvider.class);

        final RobotVariable variable = createVariable();

        final SuiteFileMarkersContainerMock markersContainer = new SuiteFileMarkersContainerMock();
        markersContainer.registerMarkerSeverity(variable, severityOfMarker);

        when(dataProvider.getRowObject(0)).thenReturn(variable);

        final MarkersLabelAccumulator accumulator = new MarkersLabelAccumulator(markersContainer, dataProvider);
        for (int col = 0; col < 5; col++) {
            final LabelStack configLabels = new LabelStack();
            accumulator.accumulateConfigLabels(configLabels, col, 0);
            assertThat(configLabels.getLabels()).containsExactly(expectedLabel);
        }
    }

    private RobotVariable createVariable() {
        final RobotSuiteFile model = createModel();
        final RobotVariable variableWithoutMarkers = model.findSection(RobotVariablesSection.class).get().getChildren().get(0);
        return variableWithoutMarkers;
    }

    private static RobotSuiteFile createModel() {
        return new RobotSuiteFileCreator().appendLine("*** Variables ***")
                .appendLine("${var}")
                .build();
    }
}