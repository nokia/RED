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
import org.rf.ide.core.testdata.model.table.keywords.KeywordTeardown;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;
import org.rf.ide.core.testdata.text.write.DumperHelper;
import org.rf.ide.core.testdata.text.write.tables.AExecutableTableElementDumper;

public class KeywordTeardownDumper extends AExecutableTableElementDumper {

    public KeywordTeardownDumper(final DumperHelper aDumpHelper) {
        super(aDumpHelper, ModelType.USER_KEYWORD_TEARDOWN);
    }

    @Override
    public RobotElementsComparatorWithPositionChangedPresave getSorter(
            final AModelElement<? extends IExecutableStepsHolder<?>> currentElement) {
        KeywordTeardown keywordTeardown = (KeywordTeardown) currentElement;
        RobotElementsComparatorWithPositionChangedPresave sorter = new RobotElementsComparatorWithPositionChangedPresave();
        final List<RobotToken> keys = new ArrayList<>();
        if (keywordTeardown.getKeywordName() != null) {
            keys.add(keywordTeardown.getKeywordName());
        }
        sorter.addPresaveSequenceForType(RobotTokenType.KEYWORD_SETTING_TEARDOWN_KEYWORD_NAME, 1, keys);
        sorter.addPresaveSequenceForType(RobotTokenType.KEYWORD_SETTING_TEARDOWN_KEYWORD_ARGUMENT, 2,
                keywordTeardown.getArguments());
        sorter.addPresaveSequenceForType(RobotTokenType.START_HASH_COMMENT, 3,
                getElementHelper().filter(keywordTeardown.getComment(), RobotTokenType.START_HASH_COMMENT));
        sorter.addPresaveSequenceForType(RobotTokenType.COMMENT_CONTINUE, 4,
                getElementHelper().filter(keywordTeardown.getComment(), RobotTokenType.COMMENT_CONTINUE));

        return sorter;
    }

}
