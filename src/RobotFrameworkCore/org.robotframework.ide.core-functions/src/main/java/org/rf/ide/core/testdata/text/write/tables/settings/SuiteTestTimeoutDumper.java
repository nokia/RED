/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.write.tables.settings;

import java.util.ArrayList;
import java.util.List;

import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.ModelType;
import org.rf.ide.core.testdata.model.table.ARobotSectionTable;
import org.rf.ide.core.testdata.model.table.RobotElementsComparatorWithPositionChangedPresave;
import org.rf.ide.core.testdata.model.table.setting.TestTimeout;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;
import org.rf.ide.core.testdata.text.write.DumperHelper;
import org.rf.ide.core.testdata.text.write.tables.ANotExecutableTableElementDumper;

public class SuiteTestTimeoutDumper extends ANotExecutableTableElementDumper {

    public SuiteTestTimeoutDumper(final DumperHelper aDumpHelper) {
        super(aDumpHelper, ModelType.SUITE_TEST_TIMEOUT);
    }

    @Override
    public RobotElementsComparatorWithPositionChangedPresave getSorter(
            AModelElement<? extends ARobotSectionTable> currentElement) {
        TestTimeout testTimeout = (TestTimeout) currentElement;

        RobotElementsComparatorWithPositionChangedPresave sorter = new RobotElementsComparatorWithPositionChangedPresave();
        List<RobotToken> keys = new ArrayList<>();
        if (testTimeout.getTimeout() != null) {
            keys.add(testTimeout.getTimeout());
        }
        sorter.addPresaveSequenceForType(RobotTokenType.SETTING_TEST_TIMEOUT_VALUE, 1, keys);
        sorter.addPresaveSequenceForType(RobotTokenType.SETTING_TEST_TIMEOUT_MESSAGE, 2,
                testTimeout.getMessageArguments());
        sorter.addPresaveSequenceForType(RobotTokenType.START_HASH_COMMENT, 3,
                getElementHelper().filter(testTimeout.getComment(), RobotTokenType.START_HASH_COMMENT));
        sorter.addPresaveSequenceForType(RobotTokenType.COMMENT_CONTINUE, 3,
                getElementHelper().filter(testTimeout.getComment(), RobotTokenType.COMMENT_CONTINUE));

        return sorter;
    }
}
