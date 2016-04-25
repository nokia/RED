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
import org.rf.ide.core.testdata.model.table.testcases.TestCaseSetup;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;
import org.rf.ide.core.testdata.text.write.DumperHelper;
import org.rf.ide.core.testdata.text.write.tables.AExecutableTableElementDumper;

public class TestCaseSetupDumper extends AExecutableTableElementDumper {

    public TestCaseSetupDumper(final DumperHelper aDumpHelper) {
        super(aDumpHelper, ModelType.TEST_CASE_SETUP);
    }

    @Override
    public RobotElementsComparatorWithPositionChangedPresave getSorter(
            final AModelElement<? extends IExecutableStepsHolder<?>> currentElement) {
        TestCaseSetup testSetup = (TestCaseSetup) currentElement;
        RobotElementsComparatorWithPositionChangedPresave sorter = new RobotElementsComparatorWithPositionChangedPresave();
        final List<RobotToken> keys = new ArrayList<>();
        if (testSetup.getKeywordName() != null) {
            keys.add(testSetup.getKeywordName());
        }
        sorter.addPresaveSequenceForType(RobotTokenType.TEST_CASE_SETTING_SETUP_KEYWORD_NAME, 1, keys);
        sorter.addPresaveSequenceForType(RobotTokenType.TEST_CASE_SETTING_SETUP_KEYWORD_ARGUMENT, 2,
                testSetup.getArguments());
        sorter.addPresaveSequenceForType(RobotTokenType.START_HASH_COMMENT, 3,
                getElementHelper().filter(testSetup.getComment(), RobotTokenType.START_HASH_COMMENT));
        sorter.addPresaveSequenceForType(RobotTokenType.COMMENT_CONTINUE, 4,
                getElementHelper().filter(testSetup.getComment(), RobotTokenType.COMMENT_CONTINUE));

        return sorter;
    }

}
