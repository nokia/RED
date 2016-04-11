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
import org.rf.ide.core.testdata.model.table.setting.SuiteTeardown;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;
import org.rf.ide.core.testdata.text.write.DumperHelper;
import org.rf.ide.core.testdata.text.write.tables.ANotExecutableTableElementDumper;

public class SuiteTeardownDumper extends ANotExecutableTableElementDumper {

    public SuiteTeardownDumper(final DumperHelper aDumpHelper) {
        super(aDumpHelper, ModelType.SUITE_TEARDOWN);
    }

    @Override
    public RobotElementsComparatorWithPositionChangedPresave getSorter(
            AModelElement<? extends ARobotSectionTable> currentElement) {
        SuiteTeardown suiteTeardown = (SuiteTeardown) currentElement;

        RobotElementsComparatorWithPositionChangedPresave sorter = new RobotElementsComparatorWithPositionChangedPresave();
        final List<RobotToken> keys = new ArrayList<>();
        if (suiteTeardown.getKeywordName() != null) {
            keys.add(suiteTeardown.getKeywordName());
        }
        sorter.addPresaveSequenceForType(RobotTokenType.SETTING_SUITE_TEARDOWN_KEYWORD_NAME, 1, keys);
        sorter.addPresaveSequenceForType(RobotTokenType.SETTING_SUITE_TEARDOWN_KEYWORD_ARGUMENT, 2,
                suiteTeardown.getArguments());
        sorter.addPresaveSequenceForType(RobotTokenType.START_HASH_COMMENT, 3,
                getElementHelper().filter(suiteTeardown.getComment(), RobotTokenType.START_HASH_COMMENT));
        sorter.addPresaveSequenceForType(RobotTokenType.COMMENT_CONTINUE, 3,
                getElementHelper().filter(suiteTeardown.getComment(), RobotTokenType.COMMENT_CONTINUE));

        return sorter;
    }
}
