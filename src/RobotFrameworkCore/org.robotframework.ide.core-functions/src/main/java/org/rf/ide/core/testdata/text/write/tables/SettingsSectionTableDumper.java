/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.write.tables;

import java.util.ArrayList;

import org.rf.ide.core.testdata.model.ModelType;
import org.rf.ide.core.testdata.model.table.ARobotSectionTable;
import org.rf.ide.core.testdata.model.table.TableHeader;
import org.rf.ide.core.testdata.text.write.DumperHelper;
import org.rf.ide.core.testdata.text.write.SectionBuilder.SectionType;

public class SettingsSectionTableDumper extends ANotExecutableTableDumper {

    private final static ModelType MY_TYPE = ModelType.SETTINGS_TABLE_HEADER;

    public SettingsSectionTableDumper(final DumperHelper aDumpHelper) {
        super(aDumpHelper, new ArrayList<ISectionElementDumper>(0));
    }

    @Override
    public boolean isServedType(final TableHeader<? extends ARobotSectionTable> header) {
        return (header.getModelType() == MY_TYPE);
    }

    @Override
    public SectionType getSectionType() {
        return SectionType.SETTINGS;
    }
}
