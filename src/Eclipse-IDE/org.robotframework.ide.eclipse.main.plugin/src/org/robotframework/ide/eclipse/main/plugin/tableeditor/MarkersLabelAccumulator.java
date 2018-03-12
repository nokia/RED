/*
* Copyright 2016 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.robotframework.ide.eclipse.main.plugin.tableeditor;

import java.util.Optional;

import org.eclipse.nebula.widgets.nattable.data.IRowDataProvider;
import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.eclipse.nebula.widgets.nattable.layer.cell.IConfigLabelAccumulator;
import org.robotframework.ide.eclipse.main.plugin.model.RobotFileInternalElement;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.ProblemCategory.Severity;


public class MarkersLabelAccumulator implements IConfigLabelAccumulator {

    public static final String TASK_LABEL = "TASK";

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

        markersContainer.getHighestSeverityMarkerFor(rowObject).ifPresent(s -> {
            if (s == Severity.INFO) {
                configLabels.addLabel(INFO_MARKER_LABEL);
            } else if (s == Severity.WARNING) {
                configLabels.addLabel(WARNING_MARKER_LABEL);
            } else {
                configLabels.addLabel(ERROR_MARKER_LABEL);
            }
        });

        if (markersContainer.hasTaskMarkerFor(rowObject)) {
            configLabels.addLabel(TASK_LABEL);
        }
    }

    protected Optional<RobotFileInternalElement> getRowModelObject(final int rowPosition) {
        final Object rowObject = dataProvider.getRowObject(rowPosition);
        return rowObject instanceof RobotFileInternalElement ? Optional.of((RobotFileInternalElement) rowObject)
                : Optional.empty();
    }
}
