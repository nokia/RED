/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.write.tables.settings;

import java.util.ArrayList;
import java.util.List;

import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.ModelType;
import org.rf.ide.core.testdata.model.table.ARobotSectionTable;
import org.rf.ide.core.testdata.model.table.RobotElementsComparatorWithPositionChangedPresave;
import org.rf.ide.core.testdata.model.table.setting.ResourceImport;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;
import org.rf.ide.core.testdata.text.write.DumperHelper;
import org.rf.ide.core.testdata.text.write.tables.ANotExecutableTableElementDumper;

public class ResourceImportDumper extends ANotExecutableTableElementDumper {

    public ResourceImportDumper(final DumperHelper aDumpHelper) {
        super(aDumpHelper, ModelType.RESOURCE_IMPORT_SETTING);
    }

    @Override
    public RobotElementsComparatorWithPositionChangedPresave getSorter(
            final AModelElement<? extends ARobotSectionTable> currentElement) {
        ResourceImport resource = (ResourceImport) currentElement;

        List<RobotToken> resourcePaths = new ArrayList<>(0);
        if (resource.getPathOrName() != null) {
            resourcePaths.add(resource.getPathOrName());
        }

        RobotElementsComparatorWithPositionChangedPresave sorter = new RobotElementsComparatorWithPositionChangedPresave();
        sorter.addPresaveSequenceForType(RobotTokenType.SETTING_RESOURCE_FILE_NAME, 1, resourcePaths);
        sorter.addPresaveSequenceForType(RobotTokenType.SETTING_RESOURCE_UNWANTED_ARGUMENT, 2,
                resource.getUnexpectedTrashArguments());
        sorter.addPresaveSequenceForType(RobotTokenType.START_HASH_COMMENT, 3,
                getElementHelper().filter(resource.getComment(), RobotTokenType.START_HASH_COMMENT));
        sorter.addPresaveSequenceForType(RobotTokenType.COMMENT_CONTINUE, 3,
                getElementHelper().filter(resource.getComment(), RobotTokenType.COMMENT_CONTINUE));

        return sorter;
    }
}
