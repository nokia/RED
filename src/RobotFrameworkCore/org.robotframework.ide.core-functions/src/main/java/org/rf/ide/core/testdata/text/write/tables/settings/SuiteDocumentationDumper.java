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
import org.rf.ide.core.testdata.model.table.setting.SuiteDocumentation;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;
import org.rf.ide.core.testdata.text.write.DumperHelper;
import org.rf.ide.core.testdata.text.write.tables.ANotExecutableTableElementDumper;

public class SuiteDocumentationDumper extends ANotExecutableTableElementDumper {

    public SuiteDocumentationDumper(final DumperHelper aDumpHelper) {
        super(aDumpHelper, ModelType.SUITE_DOCUMENTATION);
    }

    @Override
    public RobotElementsComparatorWithPositionChangedPresave getSorter(
            AModelElement<? extends ARobotSectionTable> currentElement) {
        SuiteDocumentation suiteDoc = (SuiteDocumentation) currentElement;
        RobotElementsComparatorWithPositionChangedPresave sorter = new RobotElementsComparatorWithPositionChangedPresave();
        sorter.addPresaveSequenceForType(RobotTokenType.SETTING_DOCUMENTATION_TEXT, 1, suiteDoc.getDocumentationText());
        sorter.addPresaveSequenceForType(RobotTokenType.START_HASH_COMMENT, 2,
                getElementHelper().filter(suiteDoc.getComment(), RobotTokenType.START_HASH_COMMENT));
        sorter.addPresaveSequenceForType(RobotTokenType.COMMENT_CONTINUE, 2,
                getElementHelper().filter(suiteDoc.getComment(), RobotTokenType.COMMENT_CONTINUE));

        return sorter;
    }

}
