/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.write.keywords;

import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.ModelType;
import org.rf.ide.core.testdata.model.table.IExecutableStepsHolder;
import org.rf.ide.core.testdata.model.table.RobotElementsComparatorWithPositionChangedPresave;
import org.rf.ide.core.testdata.model.table.keywords.KeywordDocumentation;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;
import org.rf.ide.core.testdata.text.write.DumperHelper;
import org.rf.ide.core.testdata.text.write.tables.AExecutableTableElementDumper;

public class KeywordDocumentationDumper extends AExecutableTableElementDumper {

    public KeywordDocumentationDumper(final DumperHelper aDumpHelper) {
        super(aDumpHelper, ModelType.USER_KEYWORD_DOCUMENTATION);
    }

    @Override
    public RobotElementsComparatorWithPositionChangedPresave getSorter(
            final AModelElement<? extends IExecutableStepsHolder<?>> currentElement) {
        KeywordDocumentation keywordDoc = (KeywordDocumentation) currentElement;
        RobotElementsComparatorWithPositionChangedPresave sorter = new RobotElementsComparatorWithPositionChangedPresave();
        sorter.addPresaveSequenceForType(RobotTokenType.KEYWORD_SETTING_DOCUMENTATION_TEXT, 1,
                keywordDoc.getDocumentationText());
        sorter.addPresaveSequenceForType(RobotTokenType.START_HASH_COMMENT, 2,
                getElementHelper().filter(keywordDoc.getComment(), RobotTokenType.START_HASH_COMMENT));
        sorter.addPresaveSequenceForType(RobotTokenType.COMMENT_CONTINUE, 2,
                getElementHelper().filter(keywordDoc.getComment(), RobotTokenType.COMMENT_CONTINUE));

        return sorter;
    }

}
