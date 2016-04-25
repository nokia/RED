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
import org.rf.ide.core.testdata.model.table.testcases.TestCaseTemplate;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;
import org.rf.ide.core.testdata.text.write.DumperHelper;
import org.rf.ide.core.testdata.text.write.tables.AExecutableTableElementDumper;

public class TestCaseTemplateDumper extends AExecutableTableElementDumper {

    public TestCaseTemplateDumper(final DumperHelper aDumpHelper) {
        super(aDumpHelper, ModelType.TEST_CASE_TEMPLATE);
    }

    @Override
    public RobotElementsComparatorWithPositionChangedPresave getSorter(
            final AModelElement<? extends IExecutableStepsHolder<?>> currentElement) {
        TestCaseTemplate testTemplate = (TestCaseTemplate) currentElement;
        RobotElementsComparatorWithPositionChangedPresave sorter = new RobotElementsComparatorWithPositionChangedPresave();
        final List<RobotToken> keys = new ArrayList<>();
        if (testTemplate.getKeywordName() != null) {
            keys.add(testTemplate.getKeywordName());
        }
        sorter.addPresaveSequenceForType(RobotTokenType.TEST_CASE_SETTING_TEMPLATE_KEYWORD_NAME, 1, keys);
        sorter.addPresaveSequenceForType(RobotTokenType.TEST_CASE_SETTING_TEMPLATE_KEYWORD_UNWANTED_ARGUMENT, 2,
                testTemplate.getUnexpectedTrashArguments());
        sorter.addPresaveSequenceForType(RobotTokenType.START_HASH_COMMENT, 3,
                getElementHelper().filter(testTemplate.getComment(), RobotTokenType.START_HASH_COMMENT));
        sorter.addPresaveSequenceForType(RobotTokenType.COMMENT_CONTINUE, 4,
                getElementHelper().filter(testTemplate.getComment(), RobotTokenType.COMMENT_CONTINUE));

        return sorter;
    }

}
