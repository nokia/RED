/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.write.tables.testcases;

import java.util.ArrayList;
import java.util.List;

import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.ModelType;
import org.rf.ide.core.testdata.model.table.IExecutableStepsHolder;
import org.rf.ide.core.testdata.model.table.RobotElementsComparatorWithPositionChangedPresave;
import org.rf.ide.core.testdata.model.table.testcases.TestCaseTimeout;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;
import org.rf.ide.core.testdata.text.write.DumperHelper;
import org.rf.ide.core.testdata.text.write.tables.AExecutableTableElementDumper;

public class TestCaseTimeoutDumper extends AExecutableTableElementDumper {

    public TestCaseTimeoutDumper(final DumperHelper aDumpHelper) {
        super(aDumpHelper, ModelType.TEST_CASE_TIMEOUT);
    }

    @Override
    public RobotElementsComparatorWithPositionChangedPresave getSorter(
            final AModelElement<? extends IExecutableStepsHolder<?>> currentElement) {
        TestCaseTimeout testTimeout = (TestCaseTimeout) currentElement;
        RobotElementsComparatorWithPositionChangedPresave sorter = new RobotElementsComparatorWithPositionChangedPresave();
        List<RobotToken> keys = new ArrayList<>();
        if (testTimeout.getTimeout() != null) {
            keys.add(testTimeout.getTimeout());
        }
        sorter.addPresaveSequenceForType(RobotTokenType.TEST_CASE_SETTING_TIMEOUT_VALUE, 1, keys);
        sorter.addPresaveSequenceForType(RobotTokenType.TEST_CASE_SETTING_TIMEOUT_MESSAGE, 2, testTimeout.getMessage());
        sorter.addPresaveSequenceForType(RobotTokenType.START_HASH_COMMENT, 3,
                getElementHelper().filter(testTimeout.getComment(), RobotTokenType.START_HASH_COMMENT));
        sorter.addPresaveSequenceForType(RobotTokenType.COMMENT_CONTINUE, 3,
                getElementHelper().filter(testTimeout.getComment(), RobotTokenType.COMMENT_CONTINUE));

        return sorter;
    }

}
