/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.write.tables.keywords;

import java.util.ArrayList;
import java.util.List;

import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.ModelType;
import org.rf.ide.core.testdata.model.table.IExecutableStepsHolder;
import org.rf.ide.core.testdata.model.table.RobotElementsComparatorWithPositionChangedPresave;
import org.rf.ide.core.testdata.model.table.RobotEmptyRow;
import org.rf.ide.core.testdata.model.table.keywords.UserKeyword;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.write.DumperHelper;
import org.rf.ide.core.testdata.text.write.tables.AExecutableTableElementDumper;
import org.rf.ide.core.testdata.text.write.tables.ForContinueStartWithCommentFixer;

public class KeywordEmptyLineDumper extends AExecutableTableElementDumper {

    public KeywordEmptyLineDumper(final DumperHelper aDumpHelper) {
        super(aDumpHelper, ModelType.USER_KEYWORD_EMPTY_LINE);
        addAfterSortTask(new ForContinueStartWithCommentFixer());
    }

    @SuppressWarnings("unchecked")
    @Override
    public RobotElementsComparatorWithPositionChangedPresave getSorter(
            final AModelElement<? extends IExecutableStepsHolder<?>> currentElement) {
        final RobotEmptyRow<UserKeyword> keywordEmptyLine = (RobotEmptyRow<UserKeyword>) currentElement;
        final RobotElementsComparatorWithPositionChangedPresave sorter = new RobotElementsComparatorWithPositionChangedPresave();

        final List<RobotToken> keys = new ArrayList<>();
        if (keywordEmptyLine.getDeclaration() != null) {
            keys.add(keywordEmptyLine.getDeclaration());
        }

        return sorter;
    }

    @Override
    public boolean isServedType(final AModelElement<? extends IExecutableStepsHolder<?>> element) {
        return super.isServedType(element);
    }
}
