/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.write.tables;

import static com.google.common.collect.Lists.newArrayList;

import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.ModelType;
import org.rf.ide.core.testdata.model.table.IExecutableStepsHolder;
import org.rf.ide.core.testdata.model.table.RobotElementsComparatorWithPositionChangedPresave;
import org.rf.ide.core.testdata.text.write.DumperHelper;

class ExecutableHolderEmptyLineDumper extends ExecutableTableElementDumper {

    ExecutableHolderEmptyLineDumper(final DumperHelper helper, final ModelType modelType) {
        super(helper, modelType, newArrayList(new ForContinueStartWithCommentFixer()));
    }

    @Override
    public RobotElementsComparatorWithPositionChangedPresave getSorter(
            final AModelElement<? extends IExecutableStepsHolder<?>> currentElement) {
        return new RobotElementsComparatorWithPositionChangedPresave();
    }
}
