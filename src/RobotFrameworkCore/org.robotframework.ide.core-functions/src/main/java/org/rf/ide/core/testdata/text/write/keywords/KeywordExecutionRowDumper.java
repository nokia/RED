/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.write.keywords;

import java.util.ArrayList;
import java.util.List;

import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.ModelType;
import org.rf.ide.core.testdata.model.table.IExecutableStepsHolder;
import org.rf.ide.core.testdata.model.table.RobotElementsComparatorWithPositionChangedPresave;
import org.rf.ide.core.testdata.model.table.RobotExecutableRow;
import org.rf.ide.core.testdata.model.table.keywords.UserKeyword;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;
import org.rf.ide.core.testdata.text.write.DumperHelper;
import org.rf.ide.core.testdata.text.write.tables.AExecutableTableElementDumper;

public class KeywordExecutionRowDumper extends AExecutableTableElementDumper {

    public KeywordExecutionRowDumper(final DumperHelper aDumpHelper) {
        super(aDumpHelper, ModelType.USER_KEYWORD_EXECUTABLE_ROW);
    }

    @SuppressWarnings("unchecked")
    @Override
    public RobotElementsComparatorWithPositionChangedPresave getSorter(
            final AModelElement<? extends IExecutableStepsHolder<?>> currentElement) {
        RobotExecutableRow<UserKeyword> userKeyword = (RobotExecutableRow<UserKeyword>) currentElement;
        RobotElementsComparatorWithPositionChangedPresave sorter = new RobotElementsComparatorWithPositionChangedPresave();

        final List<RobotToken> keys = new ArrayList<>();
        if (userKeyword.getAction() != null) {
            keys.add(userKeyword.getAction());
        }
        sorter.addPresaveSequenceForType(RobotTokenType.KEYWORD_ACTION_NAME, 1, keys);
        sorter.addPresaveSequenceForType(RobotTokenType.KEYWORD_ACTION_ARGUMENT, 2, userKeyword.getArguments());
        sorter.addPresaveSequenceForType(RobotTokenType.START_HASH_COMMENT, 2,
                getElementHelper().filter(userKeyword.getComment(), RobotTokenType.START_HASH_COMMENT));
        sorter.addPresaveSequenceForType(RobotTokenType.COMMENT_CONTINUE, 2,
                getElementHelper().filter(userKeyword.getComment(), RobotTokenType.COMMENT_CONTINUE));

        return sorter;
    }

}
