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
import org.rf.ide.core.testdata.model.table.setting.TestTeardown;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;
import org.rf.ide.core.testdata.text.write.DumperHelper;
import org.rf.ide.core.testdata.text.write.tables.ANotExecutableTableElementDumper;

public class TestTeardownDumper extends ANotExecutableTableElementDumper {

    public TestTeardownDumper(final DumperHelper aDumpHelper) {
        super(aDumpHelper, ModelType.SUITE_TEST_TEARDOWN);
    }

    @Override
    public RobotElementsComparatorWithPositionChangedPresave getSorter(
            AModelElement<? extends ARobotSectionTable> currentElement) {
        TestTeardown testTeardown = (TestTeardown) currentElement;

        RobotElementsComparatorWithPositionChangedPresave sorter = new RobotElementsComparatorWithPositionChangedPresave();
        final List<RobotToken> keys = new ArrayList<>();
        if (testTeardown.getKeywordName() != null) {
            keys.add(testTeardown.getKeywordName());
        }
        sorter.addPresaveSequenceForType(RobotTokenType.SETTING_TEST_TEARDOWN_KEYWORD_NAME, 1, keys);
        sorter.addPresaveSequenceForType(RobotTokenType.SETTING_TEST_TEARDOWN_KEYWORD_ARGUMENT, 2,
                testTeardown.getArguments());
        sorter.addPresaveSequenceForType(RobotTokenType.START_HASH_COMMENT, 3, testTeardown.getComment());

        return sorter;
    }
}
