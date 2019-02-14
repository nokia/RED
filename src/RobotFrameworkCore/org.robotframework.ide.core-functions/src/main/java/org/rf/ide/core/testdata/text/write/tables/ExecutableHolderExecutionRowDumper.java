/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.write.tables;

import static com.google.common.collect.Lists.newArrayList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.ModelType;
import org.rf.ide.core.testdata.model.table.IExecutableStepsHolder;
import org.rf.ide.core.testdata.model.table.RobotElementsComparatorWithPositionChangedPresave;
import org.rf.ide.core.testdata.model.table.RobotExecutableRow;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;
import org.rf.ide.core.testdata.text.write.DumperHelper;

class ExecutableHolderExecutionRowDumper extends ExecutableTableElementDumper {

    private final static Map<ModelType, List<RobotTokenType>> TYPES = new HashMap<>();
    static {
        TYPES.put(ModelType.TEST_CASE_EXECUTABLE_ROW,
                newArrayList(RobotTokenType.TEST_CASE_ACTION_NAME, RobotTokenType.TEST_CASE_ACTION_ARGUMENT,
                        RobotTokenType.START_HASH_COMMENT, RobotTokenType.COMMENT_CONTINUE));
        TYPES.put(ModelType.TASK_EXECUTABLE_ROW,
                newArrayList(RobotTokenType.TASK_ACTION_NAME, RobotTokenType.TASK_ACTION_ARGUMENT,
                        RobotTokenType.START_HASH_COMMENT, RobotTokenType.COMMENT_CONTINUE));
        TYPES.put(ModelType.USER_KEYWORD_EXECUTABLE_ROW,
                newArrayList(RobotTokenType.KEYWORD_ACTION_NAME, RobotTokenType.KEYWORD_ACTION_ARGUMENT,
                        RobotTokenType.START_HASH_COMMENT, RobotTokenType.COMMENT_CONTINUE));
    }

    ExecutableHolderExecutionRowDumper(final DumperHelper helper, final ModelType modelType) {
        super(helper, modelType);
    }

    @Override
    public RobotElementsComparatorWithPositionChangedPresave getSorter(
            final AModelElement<? extends IExecutableStepsHolder<?>> currentElement) {
        final RobotExecutableRow<?> userKeyword = (RobotExecutableRow<?>) currentElement;
        final RobotElementsComparatorWithPositionChangedPresave sorter = new RobotElementsComparatorWithPositionChangedPresave();

        final List<RobotTokenType> tokenTypes = TYPES.get(servedType);

        sorter.addPresaveSequenceForType(tokenTypes.get(0), 1, getAction(userKeyword));
        sorter.addPresaveSequenceForType(tokenTypes.get(1), 2, userKeyword.getArguments());
        sorter.addPresaveSequenceForType(tokenTypes.get(2), 3,
                elemUtility.filter(userKeyword.getComment(), tokenTypes.get(2)));
        sorter.addPresaveSequenceForType(tokenTypes.get(3), 4,
                elemUtility.filter(userKeyword.getComment(), tokenTypes.get(3)));

        return sorter;
    }

    private List<RobotToken> getAction(final RobotExecutableRow<?> userKeyword) {
        final List<RobotToken> keys = new ArrayList<>();
        if (userKeyword.getAction() != null) {
            keys.add(userKeyword.getAction());
        }
        return keys;
    }
}
