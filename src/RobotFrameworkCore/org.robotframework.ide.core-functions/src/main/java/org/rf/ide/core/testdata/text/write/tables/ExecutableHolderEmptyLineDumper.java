/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.write.tables;

import java.util.ArrayList;
import java.util.List;

import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.ModelType;
import org.rf.ide.core.testdata.model.table.IExecutableStepsHolder;
import org.rf.ide.core.testdata.model.table.RobotElementsComparatorWithPositionChangedPresave;
import org.rf.ide.core.testdata.model.table.RobotEmptyRow;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;
import org.rf.ide.core.testdata.text.write.DumperHelper;

class ExecutableHolderEmptyLineDumper extends ExecutableTableElementDumper {

    ExecutableHolderEmptyLineDumper(final DumperHelper helper) {
        super(helper, ModelType.EMPTY_LINE);
    }

    @Override
    public RobotElementsComparatorWithPositionChangedPresave getSorter(
            final AModelElement<? extends IExecutableStepsHolder<?>> currentElement) {
        final RobotElementsComparatorWithPositionChangedPresave sorter = new RobotElementsComparatorWithPositionChangedPresave();

        final RobotEmptyRow<?> emptyRow = (RobotEmptyRow<?>) currentElement;
        sorter.addPresaveSequenceForType(RobotTokenType.EMPTY_CELL, 1, getEmpty(emptyRow));
        sorter.addPresaveSequenceForType(RobotTokenType.START_HASH_COMMENT, 2,
                elemUtility.filter(emptyRow.getComment(), RobotTokenType.START_HASH_COMMENT));
        sorter.addPresaveSequenceForType(RobotTokenType.COMMENT_CONTINUE, 3,
                elemUtility.filter(emptyRow.getComment(), RobotTokenType.COMMENT_CONTINUE));
        return sorter;
    }

    private List<RobotToken> getEmpty(final RobotEmptyRow<?> emptyRow) {
        final List<RobotToken> keys = new ArrayList<>();
        keys.add(emptyRow.getEmptyToken());
        return keys;
    }
}
