/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.red.nattable;

import org.eclipse.nebula.widgets.nattable.data.IRowDataProvider;
import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.eclipse.nebula.widgets.nattable.layer.cell.IConfigLabelAccumulator;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.TableConfigurationLabels;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.dnd.PositionCoordinateTransfer.PositionCoordinateSerializer;

import com.google.common.base.Predicate;


public class AssistanceLabelAccumulator implements IConfigLabelAccumulator {

    private final IRowDataProvider<?> provider;

    private final Predicate<PositionCoordinateSerializer> positionPredicate;

    private final Predicate<Object> rowObjectPredicate;

    public AssistanceLabelAccumulator(final IRowDataProvider<?> provider,
            final Predicate<PositionCoordinateSerializer> positionPredicate,
            final Predicate<Object> rowObjectPredicate) {
        this.provider = provider;
        this.positionPredicate = positionPredicate;
        this.rowObjectPredicate = rowObjectPredicate;
    }

    @Override
    public void accumulateConfigLabels(final LabelStack configLabels, final int columnPosition, final int rowPosition) {
        if (positionPredicate.apply(new PositionCoordinateSerializer(columnPosition, rowPosition))
                && rowObjectPredicate.apply(provider.getRowObject(rowPosition))) {
            configLabels.addLabel(TableConfigurationLabels.ASSIST_REQUIRED);
        }
    }
}
