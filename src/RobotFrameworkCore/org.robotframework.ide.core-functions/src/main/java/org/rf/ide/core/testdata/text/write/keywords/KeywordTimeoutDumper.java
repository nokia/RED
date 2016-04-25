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
import org.rf.ide.core.testdata.model.table.keywords.KeywordTimeout;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;
import org.rf.ide.core.testdata.text.write.DumperHelper;
import org.rf.ide.core.testdata.text.write.tables.AExecutableTableElementDumper;

public class KeywordTimeoutDumper extends AExecutableTableElementDumper {

    public KeywordTimeoutDumper(final DumperHelper aDumpHelper) {
        super(aDumpHelper, ModelType.USER_KEYWORD_TIMEOUT);
    }

    @Override
    public RobotElementsComparatorWithPositionChangedPresave getSorter(
            final AModelElement<? extends IExecutableStepsHolder<?>> currentElement) {
        KeywordTimeout keywordTimeout = (KeywordTimeout) currentElement;
        RobotElementsComparatorWithPositionChangedPresave sorter = new RobotElementsComparatorWithPositionChangedPresave();
        List<RobotToken> keys = new ArrayList<>();
        if (keywordTimeout.getTimeout() != null) {
            keys.add(keywordTimeout.getTimeout());
        }
        sorter.addPresaveSequenceForType(RobotTokenType.KEYWORD_SETTING_TIMEOUT_VALUE, 1, keys);
        sorter.addPresaveSequenceForType(RobotTokenType.KEYWORD_SETTING_TIMEOUT_MESSAGE, 2,
                keywordTimeout.getMessage());
        sorter.addPresaveSequenceForType(RobotTokenType.START_HASH_COMMENT, 3,
                getElementHelper().filter(keywordTimeout.getComment(), RobotTokenType.START_HASH_COMMENT));
        sorter.addPresaveSequenceForType(RobotTokenType.COMMENT_CONTINUE, 3,
                getElementHelper().filter(keywordTimeout.getComment(), RobotTokenType.COMMENT_CONTINUE));

        return sorter;
    }

}
