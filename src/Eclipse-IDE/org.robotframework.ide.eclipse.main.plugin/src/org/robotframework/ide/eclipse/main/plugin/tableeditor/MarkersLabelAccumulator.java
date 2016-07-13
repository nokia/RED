package org.robotframework.ide.eclipse.main.plugin.tableeditor;

import org.eclipse.nebula.widgets.nattable.data.IRowDataProvider;
import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.eclipse.nebula.widgets.nattable.layer.cell.IConfigLabelAccumulator;
import org.robotframework.ide.eclipse.main.plugin.model.RobotFileInternalElement;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.IProblemCause.Severity;

import com.google.common.base.Optional;


public class MarkersLabelAccumulator implements IConfigLabelAccumulator {

    public static final String WARNING_MARKER_LABEL = "WARNING";
    public static final String ERROR_MARKER_LABEL = "ERROR";
    public static final String INFO_MARKER_LABEL = "INFO";

    private final SuiteFileMarkersContainer markersContainer;

    protected final IRowDataProvider<?> dataProvider;

    public MarkersLabelAccumulator(final SuiteFileMarkersContainer markersContainer,
            final IRowDataProvider<?> dataProvider) {
        this.markersContainer = markersContainer;
        this.dataProvider = dataProvider;
    }

    @Override
    public void accumulateConfigLabels(final LabelStack configLabels, final int columnPosition, final int rowPosition) {
        final Optional<RobotFileInternalElement> rowObject = getRowModelObject(rowPosition);

        final Optional<Severity> severity = markersContainer.getHighestSeverityMarkerFor(rowObject);
        if (severity.isPresent() && severity.get() == Severity.INFO) {
            configLabels.addLabel(INFO_MARKER_LABEL);
        } else if (severity.isPresent() && severity.get() == Severity.WARNING) {
            configLabels.addLabel(WARNING_MARKER_LABEL);
        } else if (severity.isPresent()) {
            configLabels.addLabel(ERROR_MARKER_LABEL);
        }
    }

    protected Optional<RobotFileInternalElement> getRowModelObject(final int rowPosition) {
        final Object rowObject = dataProvider.getRowObject(rowPosition);
        return rowObject instanceof RobotFileInternalElement ? Optional.of((RobotFileInternalElement) rowObject)
                : Optional.<RobotFileInternalElement> absent();
    }
}
