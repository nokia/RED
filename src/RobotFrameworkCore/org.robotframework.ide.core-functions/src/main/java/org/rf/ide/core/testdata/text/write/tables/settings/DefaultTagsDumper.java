/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.write.tables.settings;

import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.ModelType;
import org.rf.ide.core.testdata.model.table.ARobotSectionTable;
import org.rf.ide.core.testdata.model.table.RobotElementsComparatorWithPositionChangedPresave;
import org.rf.ide.core.testdata.model.table.setting.DefaultTags;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;
import org.rf.ide.core.testdata.text.write.DumperHelper;
import org.rf.ide.core.testdata.text.write.tables.ANotExecutableTableElementDumper;

public class DefaultTagsDumper extends ANotExecutableTableElementDumper {

    public DefaultTagsDumper(final DumperHelper aDumpHelper) {
        super(aDumpHelper, ModelType.DEFAULT_TAGS_SETTING);
    }

    @Override
    public RobotElementsComparatorWithPositionChangedPresave getSorter(
            AModelElement<? extends ARobotSectionTable> currentElement) {
        DefaultTags defaultTags = (DefaultTags) currentElement;
        RobotElementsComparatorWithPositionChangedPresave sorter = new RobotElementsComparatorWithPositionChangedPresave();
        sorter.addPresaveSequenceForType(RobotTokenType.SETTING_DEFAULT_TAG, 1, defaultTags.getTags());
        sorter.addPresaveSequenceForType(RobotTokenType.START_HASH_COMMENT, 2,
                getElementHelper().filter(defaultTags.getComment(), RobotTokenType.START_HASH_COMMENT));
        sorter.addPresaveSequenceForType(RobotTokenType.COMMENT_CONTINUE, 3,
                getElementHelper().filter(defaultTags.getComment(), RobotTokenType.COMMENT_CONTINUE));

        return sorter;
    }

}
