/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.mapping.collect;

import java.util.ArrayList;
import java.util.List;

import org.rf.ide.core.testdata.model.RobotFileOutput;
import org.rf.ide.core.testdata.model.table.TestCaseTable;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;

/**
 * @author wypych
 */
public class TestCasesTokenCollector implements ITableTokensCollector {

    @Override
    public List<RobotToken> collect(final RobotFileOutput outModel) {
        final List<RobotToken> tokens = new ArrayList<>(0);

        final TestCaseTable testCaseTable = outModel.getFileModel().getTestCaseTable();
        if (testCaseTable.isPresent()) {
            tokens.addAll(AModelElementElementsHelper.collect(testCaseTable.getHeaders()));
            tokens.addAll(AModelElementElementsHelper.collect(testCaseTable.getTestCases()));
        }

        return tokens;
    }
}
